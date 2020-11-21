package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzDbQueryService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.QueryException;
import org.felixrilling.musicbrainzenricher.enrichment.release.ReleaseEnricherService;
import org.felixrilling.musicbrainzenricher.history.HistoryService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
class EnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrichmentService.class);

    private final ReleaseEnricherService releaseEnricherService;
    private final HistoryService historyService;
    private final ApplicationContext applicationContext;

    EnrichmentService(ReleaseEnricherService releaseEnricherService, HistoryService historyService, ApplicationContext applicationContext) {
        this.releaseEnricherService = releaseEnricherService;
        this.historyService = historyService;
        this.applicationContext = applicationContext;
    }

    public void runInDumpMode(@NotNull DataType dataType) throws QueryException {
        MusicbrainzDbQueryService musicbrainzDbQueryService = applicationContext.getBean(MusicbrainzDbQueryService.class);

        if (dataType == DataType.RELEASE) {
            musicbrainzDbQueryService.queryReleasesWithRelationships(mbid -> enrich(DataType.RELEASE, mbid, releaseEnricherService::enrichRelease));
        }
    }

    public void runInQueryMode(@NotNull DataType dataType, @NotNull String query) throws QueryException {
        MusicbrainzQueryService musicbrainzQueryService = applicationContext.getBean(MusicbrainzQueryService.class);

        if (dataType == DataType.RELEASE) {
            ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
            includes.excludeAll();
            musicbrainzQueryService.queryReleases(query, includes, mbid -> enrich(DataType.RELEASE, mbid, releaseEnricherService::enrichRelease));
        }
    }

    private void enrich(@NotNull DataType dataType, @NotNull String mbid, @NotNull MbidConsumer mbidConsumer) {
        if (!historyService.checkIsDue(dataType, mbid)) {
            logger.debug("Check is not due for '{}' ({}), skipping.", mbid, dataType);
            return;
        }
        logger.info("Starting enrichment for '{}' ({}).", mbid, dataType);
        try {
            mbidConsumer.consume(mbid);
        } catch (Exception e) {
            logger.error("Could not enrich {}' ({}).", mbid, dataType, e);
            return;
        }
        logger.info("Completed enrichment for '{}' ({}).", mbid, dataType);
        historyService.markAsChecked(dataType, mbid);
    }

    @FunctionalInterface
    private interface MbidConsumer {
        void consume(String mbid) throws Exception;
    }
}
