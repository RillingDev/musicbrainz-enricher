package org.felixrilling.musicbrainzenricher.enrichment.release;

import org.felixrilling.musicbrainzenricher.DataType;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.QueryException;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseEnrichmentService implements EnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzEditService musicbrainzEditService;

    private final @NotNull Set<Enricher> releaseEnrichers;

    ReleaseEnrichmentService(ApplicationContext applicationContext, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzEditService musicbrainzEditService) {
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzEditService = musicbrainzEditService;

        releaseEnrichers = applicationContext
                .getBeansOfType(Enricher.class).values().stream()
                .filter(enricher -> enricher.dataTypeFits(DataType.RELEASE))
                .collect(Collectors.toSet());
    }

    @Override
    public void enrichRelease(@NotNull String mbid) throws QueryException {
        ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        includes.setReleaseGroups(true);
        ReleaseWs2 releaseEntity = musicbrainzQueryService.lookUpRelease(mbid, includes);

        logger.debug("Loaded release data: '{}'.", releaseEntity);
        ReleaseEnrichmentResult result = new ReleaseEnrichmentResult();
        for (RelationWs2 relation : releaseEntity.getRelationList().getRelations()) {
            enrichForRelation(releaseEntity, relation, result);
        }
        updateEntity(releaseEntity, result);
    }

    private void enrichForRelation(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull ReleaseEnrichmentResult result) {
        boolean atLeastOneEnricherCompleted = false;
        for (Enricher enricher : releaseEnrichers) {
            if (enricher.relationFits(relation)) {
                atLeastOneEnricherCompleted = true;
                executeEnrichment(releaseEntity, relation, enricher, result);
            }
        }
        if (!atLeastOneEnricherCompleted) {
            logger.debug("Could not find any enricher for '{}'.", relation.getTargetId());
        }
    }

    private void executeEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull Enricher enricher, @NotNull ReleaseEnrichmentResult result) {
        if (enricher instanceof GenreEnricher) {
            executeGenreEnrichment(releaseEntity, relation, (GenreEnricher) enricher, result);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull GenreEnricher releaseEnricher, @NotNull ReleaseEnrichmentResult result) {
        ReleaseGroupWs2 releaseGroup = releaseEntity.getReleaseGroup();
        Set<String> oldTags = releaseGroup.getTags().stream().map(TagWs2::getName).collect(Collectors.toSet());

        Set<String> foundTags = releaseEnricher.fetchGenres(relation.getTargetId());
        logger.debug("Enricher '{}' found genres '{}' (Old: '{}') for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, oldTags, releaseGroup.getId());

        result.getNewGenres().addAll(foundTags.stream().filter(foundTag -> !oldTags.contains(foundTag)).collect(Collectors.toSet()));
    }

    private void updateEntity(@NotNull ReleaseWs2 releaseEntity, @NotNull ReleaseEnrichmentResult result) {
        if (!result.getNewGenres().isEmpty()) {
            ReleaseGroupWs2 releaseGroup = releaseEntity.getReleaseGroup();
            logger.info("Submitting new tags '{}' for release group '{}'.", result.getNewGenres(), releaseGroup
                    .getId());
            try {
                musicbrainzEditService.addReleaseGroupUserTags(releaseGroup.getId(), result.getNewGenres());
            } catch (MBWS2Exception e) {
                logger.error("Could not submit tags.", e);
            }
        }
    }

    private static class ReleaseEnrichmentResult {
        private final Set<String> newGenres = new HashSet<>();

        public @NotNull Set<String> getNewGenres() {
            return newGenres;
        }
    }
}
