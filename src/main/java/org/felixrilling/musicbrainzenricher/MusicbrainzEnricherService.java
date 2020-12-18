package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzAutoQueryService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.core.history.HistoryService;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MusicbrainzEnricherService {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

    private final ApplicationContext applicationContext;
    private final MusicbrainzAutoQueryService musicbrainzAutoQueryService;
    private final HistoryService historyService;

    MusicbrainzEnricherService(ApplicationContext applicationContext, MusicbrainzAutoQueryService musicbrainzAutoQueryService, HistoryService historyService) {
        this.applicationContext = applicationContext;
        this.musicbrainzAutoQueryService = musicbrainzAutoQueryService;
        this.historyService = historyService;
    }

    public void runInAutoQueryMode(@NotNull DataType dataType) {
        switch (dataType) {
            case RELEASE:
                musicbrainzAutoQueryService
                        .autoQueryReleasesWithRelationships(mbid -> executeEnrichment(dataType, mbid, findFittingEnrichmentService(dataType)));
                break;
            case RELEASE_GROUP:
                musicbrainzAutoQueryService
                        .autoQueryReleaseGroupsWithRelationships(mbid -> executeEnrichment(dataType, mbid, findFittingEnrichmentService(dataType)));
                break;
        }
    }

    public void runInSingleMode(@NotNull DataType dataType, @NotNull String mbid) {
        executeEnrichment(dataType, mbid, findFittingEnrichmentService(dataType));
    }

    private void executeEnrichment(@NotNull DataType dataType, @NotNull String mbid, @NotNull EnrichmentService enrichmentService) {
        if (!historyService.checkIsDue(dataType, mbid)) {
            logger.debug("Check is not due for '{}' ({}), skipping.", mbid, dataType);
            return;
        }
        logger.info("Starting enrichment for '{}' ({}).", mbid, dataType);
        try {
            enrichmentService.enrich(mbid);
        } catch (Exception e) {
            logger.error("Could not enrich {}' ({}).", mbid, dataType, e);
            return;
        }
        logger.info("Completed enrichment for '{}' ({}).", mbid, dataType);
        historyService.markAsChecked(dataType, mbid);
    }

    private @NotNull EnrichmentService findFittingEnrichmentService(@NotNull DataType dataType) {
        return applicationContext
                .getBeansOfType(EnrichmentService.class).values().stream()
                .filter(enrichmentService -> enrichmentService.getDataType().equals(dataType))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No enrichment service exists for data type '" + dataType + "'."));
    }
}
