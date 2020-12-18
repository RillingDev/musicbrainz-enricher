package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzAutoQueryService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.core.history.HistoryService;
import org.felixrilling.musicbrainzenricher.enrichment.release.ReleaseEnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.releasegroup.ReleaseGroupEnrichmentService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MusicbrainzEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

    private final ReleaseEnrichmentService releaseEnrichmentService;
    private final ReleaseGroupEnrichmentService releaseGroupEnrichmentService;
    private final MusicbrainzAutoQueryService musicbrainzAutoQueryService;
    private final HistoryService historyService;

    MusicbrainzEnricherService(ReleaseEnrichmentService releaseEnrichmentService, ReleaseGroupEnrichmentService releaseGroupEnrichmentService, MusicbrainzAutoQueryService musicbrainzAutoQueryService, HistoryService historyService) {
        this.releaseEnrichmentService = releaseEnrichmentService;
        this.releaseGroupEnrichmentService = releaseGroupEnrichmentService;
        this.musicbrainzAutoQueryService = musicbrainzAutoQueryService;
        this.historyService = historyService;
    }

    public void runInAutoQueryMode(@NotNull DataType dataType) {
        switch (dataType) {
            case RELEASE:
                musicbrainzAutoQueryService.autoQueryReleasesWithRelationships(mbid -> enrich(DataType.RELEASE, mbid, releaseEnrichmentService::enrich));
                break;
            case RELEASE_GROUP:
                musicbrainzAutoQueryService
                        .autoQueryReleaseGroupsWithRelationships(mbid -> enrich(DataType.RELEASE_GROUP, mbid, releaseGroupEnrichmentService::enrich));
                break;
        }
    }

    public void runInSingleMode(@NotNull DataType dataType, @NotNull String mbid) {
        switch (dataType) {
            case RELEASE:
                enrich(DataType.RELEASE, mbid, releaseEnrichmentService::enrich);
                break;
            case RELEASE_GROUP:
                enrich(DataType.RELEASE_GROUP, mbid, releaseGroupEnrichmentService::enrich);
                break;
        }
    }

    private void enrich(@NotNull DataType dataType, @NotNull String mbid, @NotNull Consumer<String> mbidConsumer) {
        if (!historyService.checkIsDue(dataType, mbid)) {
            logger.debug("Check is not due for '{}' ({}), skipping.", mbid, dataType);
            return;
        }
        logger.info("Starting enrichment for '{}' ({}).", mbid, dataType);
        try {
            mbidConsumer.accept(mbid);
        } catch (Exception e) {
            logger.error("Could not enrich {}' ({}).", mbid, dataType, e);
            return;
        }
        logger.info("Completed enrichment for '{}' ({}).", mbid, dataType);
        historyService.markAsChecked(dataType, mbid);
    }
}
