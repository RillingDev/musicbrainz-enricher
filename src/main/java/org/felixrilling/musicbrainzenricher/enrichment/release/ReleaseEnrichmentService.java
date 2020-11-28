package org.felixrilling.musicbrainzenricher.enrichment.release;

import org.apache.commons.collections4.CollectionUtils;
import org.felixrilling.musicbrainzenricher.DataType;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.enrichment.CoreEnrichmentService;
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
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseEnrichmentService implements EnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzEditService musicbrainzEditService;

    private final @NotNull Set<Enricher> enrichers;

    ReleaseEnrichmentService(CoreEnrichmentService coreEnrichmentService, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzEditService musicbrainzEditService) {
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzEditService = musicbrainzEditService;

        enrichers = coreEnrichmentService.findFittingEnrichers(this);
    }

    @Override
    public void enrich(@NotNull String mbid) {
        ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        includes.setReleaseGroups(true);
        Optional<ReleaseWs2> releaseOptional = musicbrainzQueryService.lookUpRelease(mbid, includes);

        if (releaseOptional.isEmpty()) {
            logger.warn("Could not find release '{}'.", mbid);
            return;
        }

        ReleaseWs2 release = releaseOptional.get();
        logger.trace("Loaded release data: '{}'.", release);
        ReleaseEnrichmentResult result = new ReleaseEnrichmentResult();
        for (RelationWs2 relation : release.getRelationList().getRelations()) {
            boolean atLeastOneEnricherCompleted = false;
            for (Enricher enricher : enrichers) {
                if (enricher.relationSupported(relation)) {
                    atLeastOneEnricherCompleted = true;
                    executeEnrichment(release, relation, enricher, result);
                }
            }
            if (!atLeastOneEnricherCompleted) {
                logger.debug("Could not find any enricher for '{}'.", relation.getTargetId());
            }
        }
        updateEntity(release, result);
    }

    private void executeEnrichment(@NotNull ReleaseWs2 release, @NotNull RelationWs2 relation, @NotNull Enricher enricher, @NotNull ReleaseEnrichmentResult result) {
        if (enricher instanceof GenreEnricher) {
            executeGenreEnrichment(release, relation, (GenreEnricher) enricher, result);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseWs2 release, @NotNull RelationWs2 relation, @NotNull GenreEnricher releaseEnricher, @NotNull ReleaseEnrichmentResult result) {
        ReleaseGroupWs2 releaseGroup = release.getReleaseGroup();
        Set<String> existingTags = releaseGroup.getTags().stream().map(TagWs2::getName).collect(Collectors.toSet());

        Set<String> foundTags = releaseEnricher.fetchGenres(relation);
        logger.debug("Enricher '{}' found genres '{}' (Existing: '{}') for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, existingTags, releaseGroup.getId());

        Collection<String> newTags = CollectionUtils.subtract(foundTags, existingTags);
        result.getNewGenres().addAll(newTags);
    }

    private void updateEntity(@NotNull ReleaseWs2 release, @NotNull ReleaseEnrichmentResult result) {
        if (!result.getNewGenres().isEmpty()) {
            ReleaseGroupWs2 releaseGroup = release.getReleaseGroup();
            logger.info("Submitting new tags '{}' for release group '{}'.", result.getNewGenres(), releaseGroup
                    .getId());
            try {
                musicbrainzEditService.addReleaseGroupUserTags(releaseGroup.getId(), result.getNewGenres());
            } catch (MBWS2Exception e) {
                logger.error("Could not submit tags.", e);
            }
        }
    }

    @Override
    public @NotNull DataType getDataType() {
        return DataType.RELEASE;
    }

    private static class ReleaseEnrichmentResult {
        private final Set<String> newGenres = new HashSet<>();

        public @NotNull Set<String> getNewGenres() {
            return newGenres;
        }
    }

}
