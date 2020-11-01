package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.MusicbrainzService;
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

    private final Set<ReleaseEnricher> releaseEnrichers;
    private final MusicbrainzService musicbrainzService;

    public ReleaseEnricherService(ApplicationContext applicationContext, MusicbrainzService musicbrainzService) {
        this.musicbrainzService = musicbrainzService;
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
            releaseEntity = musicbrainzService.lookUpRelease(mbid, includes);
        } catch (MBWS2Exception e) {
            throw new IOException(e);
        }

        Set<ReleaseEnricher> availableEnrichers = new HashSet<>(releaseEnrichers);
        for (RelationWs2 relation : releaseEntity.getRelationList().getRelations()) {
            for (ReleaseEnricher releaseEnricher : availableEnrichers) {
                if (releaseEnricher.relationFits(relation)) {
                    executeEnrichment(releaseEntity, relation, releaseEnricher);
                    availableEnrichers.remove(releaseEnricher);
                    break;
                }
            }
        }
    }

    private void executeEnrichment(ReleaseWs2 releaseEntity, RelationWs2 relation, ReleaseEnricher releaseEnricher) throws Exception {
        if (releaseEnricher instanceof GenreReleaseEnricher) {
            executeGenreEnrichment(releaseEntity, relation, (GenreReleaseEnricher) releaseEnricher);
        }
    }

    private void executeGenreEnrichment(ReleaseWs2 releaseEntity, RelationWs2 relation, GenreReleaseEnricher releaseEnricher) throws Exception {
        ReleaseGroupWs2 releaseGroup = releaseEntity.getReleaseGroup();
        Set<String> oldTags = releaseGroup.getUserTags().stream().map(TagWs2::getName).collect(Collectors.toSet());

        Set<String> foundTags = releaseEnricher.fetchGenres(relation.getTargetId());
        logger.info("Enricher '{}' found genres '{}' (Old: '{}') for release '{}'.", releaseEnricher
                .getClass().getSimpleName(), foundTags, oldTags, releaseEntity
                .getTitle());

        Set<String> newTags = foundTags.stream().filter(newTag -> !oldTags.contains(newTag)).collect(Collectors.toSet());
        if (newTags.isEmpty()) {
            logger.info("No new tags for release '{}'.", releaseEntity.getTitle());
            return;
        }
        logger.info("Submitting new tags '{}' for release '{}'.", newTags, releaseEntity
                .getTitle());
        musicbrainzService.addReleaseGroupUserTags(releaseGroup.getId(), newTags);
    }
}
