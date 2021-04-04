package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.enrichment.CoreEnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ReleaseGroupEnrichmentService implements EnrichmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupEnrichmentService.class);

    private final CoreEnrichmentService coreEnrichmentService;
    private final MusicbrainzLookupService musicbrainzLookupService;
    private final MusicbrainzEditService musicbrainzEditService;

    ReleaseGroupEnrichmentService(CoreEnrichmentService coreEnrichmentService, MusicbrainzLookupService musicbrainzLookupService, MusicbrainzEditService musicbrainzEditService) {
        this.coreEnrichmentService = coreEnrichmentService;
        this.musicbrainzLookupService = musicbrainzLookupService;
        this.musicbrainzEditService = musicbrainzEditService;
    }

    @Override
    public void enrich(@NotNull UUID mbid) {
        ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        Optional<ReleaseGroupWs2> releaseGroupOptional = musicbrainzLookupService.lookUpReleaseGroup(mbid, includes);

        if (releaseGroupOptional.isEmpty()) {
            LOGGER.warn("Could not find release group '{}'.", mbid);
            return;
        }

        ReleaseGroupWs2 releaseGroup = releaseGroupOptional.get();
        LOGGER.trace("Loaded release group data: '{}'.", releaseGroup);
        ReleaseGroupEnrichmentResult result = new ReleaseGroupEnrichmentResult();
        for (RelationWs2 relation : releaseGroup.getRelationList().getRelations()) {
            boolean atLeastOneEnricherCompleted = false;
            for (Enricher enricher : coreEnrichmentService.findFittingEnrichers(this)) {
                if (enricher.isRelationSupported(relation)) {
                    atLeastOneEnricherCompleted = true;
                    executeEnrichment(releaseGroup, relation, enricher, result);
                }
            }
            if (!atLeastOneEnricherCompleted) {
                LOGGER.debug("Could not find any enricher for '{}'.", relation.getTargetId());
            }
        }
        updateEntity(releaseGroup, result);
    }

    private void executeEnrichment(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull RelationWs2 relation, @NotNull Enricher enricher, @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult result) {
        if (enricher instanceof GenreEnricher) {
            executeGenreEnrichment(releaseGroup, relation, (GenreEnricher) enricher, result);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull RelationWs2 relation, @NotNull GenreEnricher releaseEnricher, @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult result) {
        Set<String> foundTags = releaseEnricher.fetchGenres(relation);
        LOGGER.debug("Enricher '{}' found genres '{}' for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, releaseGroup.getId());

        result.getNewGenres().addAll(foundTags);
    }

    private void updateEntity(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull ReleaseGroupEnrichmentResult result) {
        if (!result.getNewGenres().isEmpty()) {
            LOGGER.info("Submitting new tags '{}' for release group '{}'.", result.getNewGenres(), releaseGroup
                    .getId());
            try {
                musicbrainzEditService.addReleaseGroupUserTags(UUID.fromString(releaseGroup.getId()), result.getNewGenres());
            } catch (MBWS2Exception e) {
                LOGGER.error("Could not submit tags.", e);
            }
        }
    }

    @Override
    public @NotNull DataType getDataType() {
        return DataType.RELEASE_GROUP;
    }

    private static class ReleaseGroupEnrichmentResult {
        private final Set<String> newGenres = new HashSet<>();

        @NotNull Set<String> getNewGenres() {
            return newGenres;
        }
    }
}
