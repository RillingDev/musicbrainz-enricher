package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.io.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.io.musicbrainz.MusicbrainzQueryService;
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseEnricherService.class);

    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzEditService musicbrainzEditService;

    private final @NotNull Set<ReleaseEnricher> releaseEnrichers;

    ReleaseEnricherService(ApplicationContext applicationContext, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzEditService musicbrainzEditService) {
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzEditService = musicbrainzEditService;

        releaseEnrichers = new HashSet<>(applicationContext
                .getBeansOfType(ReleaseEnricher.class).values());
    }

    public void enrichRelease(@NotNull String mbid) throws Exception {
        ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
        includes.setUrlRelations(true);
        includes.setTags(true);
        includes.setUserTags(true);
        includes.setReleaseGroups(true);
        ReleaseWs2 releaseEntity;
        try {
            releaseEntity = musicbrainzQueryService.lookUpRelease(mbid, includes);
        } catch (MBWS2Exception e) {
            throw new IOException(e);
        }

        logger.info("Starting enrichment for '{}'.", releaseEntity.getId());
        ReleaseEnrichmentResult result = new ReleaseEnrichmentResult();
        for (RelationWs2 relation : releaseEntity.getRelationList().getRelations()) {
            enrichForRelation(releaseEntity, relation, result);
        }
        updateEntity(releaseEntity, result);
        logger.info("Completed enrichment for '{}'.", releaseEntity.getId());
    }

    private void enrichForRelation(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull ReleaseEnrichmentResult result) throws Exception {
        boolean atLeastOneEnricherCompleted = false;
        for (ReleaseEnricher releaseEnricher : releaseEnrichers) {
            if (releaseEnricher.relationFits(relation)) {
                atLeastOneEnricherCompleted = true;
                executeEnrichment(releaseEntity, relation, releaseEnricher, result);
            }
        }
        if (!atLeastOneEnricherCompleted) {
            logger.debug("Could not find any enricher for '{}'.", relation.getTargetId());
        }
    }

    private void executeEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull ReleaseEnricher releaseEnricher, @NotNull ReleaseEnrichmentResult result) throws Exception {
        if (releaseEnricher instanceof GenreReleaseEnricher) {
            executeGenreEnrichment(releaseEntity, relation, (GenreReleaseEnricher) releaseEnricher, result);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull GenreReleaseEnricher releaseEnricher, @NotNull ReleaseEnrichmentResult result) throws Exception {
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
