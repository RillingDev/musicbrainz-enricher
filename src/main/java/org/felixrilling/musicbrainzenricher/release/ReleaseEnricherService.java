package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.io.MusicBrainzExecutor;
import org.felixrilling.musicbrainzenricher.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.musicbrainz.MusicbrainzQueryService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseEnricherService.class);

    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzEditService musicbrainzEditService;
    private final MusicBrainzExecutor musicBrainzExecutor;

    private final @NotNull Set<ReleaseEnricher> releaseEnrichers;

    ReleaseEnricherService(ApplicationContext applicationContext, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzEditService musicbrainzEditService, MusicBrainzExecutor musicBrainzExecutor) {
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzEditService = musicbrainzEditService;
        this.musicBrainzExecutor = musicBrainzExecutor;

        Map<String, ReleaseEnricher> enricherMap = applicationContext
                .getBeansOfType(ReleaseEnricher.class);
        releaseEnrichers = new HashSet<>(enricherMap.values());
    }

    public void enrichRelease(String mbid) throws Exception {
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

        Set<ReleaseEnricher> availableEnrichers = new HashSet<>(releaseEnrichers);
        relationLoop:
        for (RelationWs2 relation : releaseEntity.getRelationList().getRelations()) {
            for (ReleaseEnricher releaseEnricher : availableEnrichers) {
                if (releaseEnricher.relationFits(relation)) {
                    executeEnrichment(releaseEntity, relation, releaseEnricher);
                    availableEnrichers.remove(releaseEnricher);
                    continue relationLoop;
                }
            }
            logger.debug("No fitting enricher found for release '{}'.", relation.getTargetId());
        }
        logger.debug("Completed enrichment for '{}'.", releaseEntity.getId());
    }

    private void executeEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, ReleaseEnricher releaseEnricher) throws Exception {
        if (releaseEnricher instanceof GenreReleaseEnricher) {
            executeGenreEnrichment(releaseEntity, relation, (GenreReleaseEnricher) releaseEnricher);
        }
    }

    private void executeGenreEnrichment(@NotNull ReleaseWs2 releaseEntity, @NotNull RelationWs2 relation, @NotNull GenreReleaseEnricher releaseEnricher) throws Exception {
        ReleaseGroupWs2 releaseGroup = releaseEntity.getReleaseGroup();
        Set<String> oldTags = releaseGroup.getTags().stream().map(TagWs2::getName).collect(Collectors.toSet());

        Set<String> foundTags = releaseEnricher.fetchGenres(relation.getTargetId());
        logger.info("Enricher '{}' found genres '{}' (Old: '{}') for release group '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, oldTags, releaseGroup.getId());

        Set<String> newTags = foundTags.stream().filter(newTag -> !oldTags.contains(newTag)).collect(Collectors.toSet());
        if (newTags.isEmpty()) {
            logger.info("No new tags for release group'{}'.", releaseGroup.getId());
            return;
        }
        musicBrainzExecutor.submit(() -> {
            logger.info("Submitting new tags '{}' for release group '{}'.", newTags, releaseGroup
                    .getId());
            try {
                musicbrainzEditService.addReleaseGroupUserTags(releaseGroup.getId(), newTags);
            } catch (MBWS2Exception e) {
                logger.error("Could not submit tags.", e);
            }
            return null;
        });
    }
}
