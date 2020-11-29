package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzDbQueryService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.enrichment.release.ReleaseEnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.releasegroup.ReleaseGroupEnrichmentService;
import org.felixrilling.musicbrainzenricher.history.HistoryService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
class MusicbrainzEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

    private final ReleaseEnrichmentService releaseEnrichmentService;
    private final ReleaseGroupEnrichmentService releaseGroupEnrichmentService;
    private final MusicbrainzQueryService musicbrainzQueryService;
    private final MusicbrainzDbQueryService musicbrainzDbQueryService;
    private final HistoryService historyService;

    MusicbrainzEnricherService(ReleaseEnrichmentService releaseEnrichmentService, ReleaseGroupEnrichmentService releaseGroupEnrichmentService, MusicbrainzQueryService musicbrainzQueryService, MusicbrainzDbQueryService musicbrainzDbQueryService, HistoryService historyService) {
        this.releaseEnrichmentService = releaseEnrichmentService;
        this.releaseGroupEnrichmentService = releaseGroupEnrichmentService;
        this.musicbrainzQueryService = musicbrainzQueryService;
        this.musicbrainzDbQueryService = musicbrainzDbQueryService;
        this.historyService = historyService;
    }

    public void runInFullMode(@NotNull DataType dataType) {
        switch (dataType) {
            case RELEASE:
                musicbrainzDbQueryService.queryReleasesWithRelationships(mbid -> enrich(DataType.RELEASE, mbid, releaseEnrichmentService::enrich));
                break;
            case RELEASE_GROUP:
                musicbrainzDbQueryService
                        .queryReleaseGroupsWithRelationships(mbid -> enrich(DataType.RELEASE_GROUP, mbid, releaseGroupEnrichmentService::enrich));
                break;
        }
    }

    public void runInQueryMode(@NotNull DataType dataType, @NotNull String query) {
        switch (dataType) {
            case RELEASE:
                ReleaseIncludesWs2 releaseIncludesWs2 = new ReleaseIncludesWs2();
                releaseIncludesWs2.excludeAll();
                musicbrainzQueryService.queryReleases(query, releaseIncludesWs2, mbid -> enrich(DataType.RELEASE, mbid, releaseEnrichmentService::enrich));
                break;
            case RELEASE_GROUP:
                ReleaseGroupIncludesWs2 releaseGroupIncludesWs2 = new ReleaseGroupIncludesWs2();
                releaseGroupIncludesWs2.excludeAll();
                musicbrainzQueryService
                        .queryReleaseGroups(query, releaseGroupIncludesWs2, mbid -> enrich(DataType.RELEASE_GROUP, mbid, releaseGroupEnrichmentService::enrich));
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
