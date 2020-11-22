package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.apache.commons.collections4.CollectionUtils;
import org.felixrilling.musicbrainzenricher.DataType;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseGroupEnrichmentService implements EnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseGroupEnrichmentService.class);

    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzEditService musicbrainzEditService;

    private final @NotNull Set<Enricher> releaseGroupEnrichers;

    ReleaseGroupEnrichmentService(ApplicationContext applicationContext, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzEditService musicbrainzEditService) {
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzEditService = musicbrainzEditService;

        releaseGroupEnrichers = applicationContext
                .getBeansOfType(Enricher.class).values().stream()
                .filter(enricher -> enricher.dataTypeSupported(DataType.RELEASE_GROUP))
                .collect(Collectors.toSet());
    }

    @Override
    public void enrich(@NotNull String mbid) {
        ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        Optional<ReleaseGroupWs2> releaseGroupOptional = musicbrainzQueryService.lookUpReleaseGroup(mbid, includes);

        if (releaseGroupOptional.isEmpty()) {
            logger.warn("Could not find release group '{}'.", mbid);
            return;
        }

        ReleaseGroupWs2 releaseGroup = releaseGroupOptional.get();
        logger.trace("Loaded release group data: '{}'.", releaseGroup);
        ReleaseGroupEnrichmentResult result = new ReleaseGroupEnrichmentResult();
        for (RelationWs2 relation : releaseGroup.getRelationList().getRelations()) {
            enrichForRelation(releaseGroup, relation, result);
        }
        updateEntity(releaseGroup, result);
    }

    private void enrichForRelation(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull RelationWs2 relation, @NotNull ReleaseGroupEnrichmentResult result) {
        boolean atLeastOneEnricherCompleted = false;
        for (Enricher enricher : releaseGroupEnrichers) {
            if (enricher.relationSupported(relation)) {
                atLeastOneEnricherCompleted = true;
                executeEnrichment(releaseGroup, relation, enricher, result);
            }
        }
        if (!atLeastOneEnricherCompleted) {
            logger.debug("Could not find any enricher for '{}'.", relation.getTargetId());
        }
    }

    private void executeEnrichment(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull RelationWs2 relation, @NotNull Enricher enricher, @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult result) {
        if (enricher instanceof GenreEnricher) {
            executeGenreEnrichment(releaseGroup, relation, (GenreEnricher) enricher, result);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull RelationWs2 relation, @NotNull GenreEnricher releaseEnricher, @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult result) {
        Set<String> oldTags = releaseGroup.getTags().stream().map(TagWs2::getName).collect(Collectors.toSet());

        Set<String> foundTags = releaseEnricher.fetchGenres(relation);
        logger.debug("Enricher '{}' found genres '{}' (Old: '{}') for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, oldTags, releaseGroup.getId());
        result.getNewGenres().addAll(CollectionUtils.subtract(foundTags, oldTags));
    }

    private void updateEntity(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull ReleaseGroupEnrichmentResult result) {
        if (!result.getNewGenres().isEmpty()) {
            logger.info("Submitting new tags '{}' for release group '{}'.", result.getNewGenres(), releaseGroup
                    .getId());
            try {
                musicbrainzEditService.addReleaseGroupUserTags(releaseGroup.getId(), result.getNewGenres());
            } catch (MBWS2Exception e) {
                logger.error("Could not submit tags.", e);
            }
        }
    }

    private static class ReleaseGroupEnrichmentResult {
        private final Set<String> newGenres = new HashSet<>();

        public @NotNull Set<String> getNewGenres() {
            return newGenres;
        }
    }
}
