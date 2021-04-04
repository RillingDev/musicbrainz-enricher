package org.felixrilling.musicbrainzenricher.enrichment.release;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.enrichment.CoreEnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ReleaseEnrichmentService implements EnrichmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

    private final CoreEnrichmentService coreEnrichmentService;
    private final MusicbrainzLookupService musicbrainzLookupService;
    private final MusicbrainzEditService musicbrainzEditService;

    ReleaseEnrichmentService(CoreEnrichmentService coreEnrichmentService, MusicbrainzLookupService musicbrainzLookupService, MusicbrainzEditService musicbrainzEditService) {
        this.coreEnrichmentService = coreEnrichmentService;
        this.musicbrainzLookupService = musicbrainzLookupService;
        this.musicbrainzEditService = musicbrainzEditService;
    }

    @Override
    public void enrich(@NotNull UUID mbid) {
        ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        includes.setReleaseGroups(true);
        Optional<ReleaseWs2> releaseOptional = musicbrainzLookupService.lookUpRelease(mbid, includes);

        if (releaseOptional.isEmpty()) {
            LOGGER.warn("Could not find release '{}'.", mbid);
            return;
        }

        ReleaseWs2 release = releaseOptional.get();
        LOGGER.trace("Loaded release data: '{}'.", release);
        ReleaseEnrichmentResult result = new ReleaseEnrichmentResult();
        for (RelationWs2 relation : release.getRelationList().getRelations()) {
            boolean atLeastOneEnricherCompleted = false;
            for (Enricher enricher : coreEnrichmentService.findFittingEnrichers(this)) {
                if (enricher.isRelationSupported(relation)) {
                    atLeastOneEnricherCompleted = true;
                    executeEnrichment(release, relation, enricher, result);
                }
            }
            if (!atLeastOneEnricherCompleted) {
                LOGGER.debug("Could not find any enricher for '{}'.", relation.getTargetId());
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

        Set<String> foundTags = releaseEnricher.fetchGenres(relation);
        LOGGER.debug("Enricher '{}' found genres '{}' for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, releaseGroup.getId());
        result.getNewGenres().addAll(foundTags);
    }

    private void updateEntity(@NotNull ReleaseWs2 release, @NotNull ReleaseEnrichmentResult result) {
        if (!result.getNewGenres().isEmpty()) {
            ReleaseGroupWs2 releaseGroup = release.getReleaseGroup();
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
        return DataType.RELEASE;
    }

    private static class ReleaseEnrichmentResult {
        private final Set<String> newGenres = new HashSet<>(10);

        @NotNull Set<String> getNewGenres() {
            return newGenres;
        }
    }

}
