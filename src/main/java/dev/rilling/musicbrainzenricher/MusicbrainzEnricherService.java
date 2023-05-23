package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzAutoQueryService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.history.HistoryService;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MusicbrainzEnricherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

	private final ApplicationContext applicationContext;
	private final MusicbrainzAutoQueryService musicbrainzAutoQueryService;
	private final HistoryService historyService;

	MusicbrainzEnricherService(ApplicationContext applicationContext,
							   MusicbrainzAutoQueryService musicbrainzAutoQueryService,
							   HistoryService historyService) {
		this.applicationContext = applicationContext;
		this.musicbrainzAutoQueryService = musicbrainzAutoQueryService;
		this.historyService = historyService;
	}

	public void runInAutoQueryMode(@NotNull DataType dataType) {
		switch (dataType) {
			case RELEASE -> musicbrainzAutoQueryService.autoQueryReleases(mbid -> executeEnrichment(
				dataType,
				mbid,
				findFittingEnrichmentService(dataType)));
			case RELEASE_GROUP -> musicbrainzAutoQueryService.autoQueryReleaseGroups(mbid -> executeEnrichment(dataType,
				mbid,
				findFittingEnrichmentService(dataType)));
		}
	}

	public void runInSingleMode(@NotNull DataType dataType, @NotNull UUID mbid) {
		executeEnrichment(dataType, mbid, findFittingEnrichmentService(dataType));
	}

	private void executeEnrichment(@NotNull DataType dataType,
								   @NotNull UUID mbid,
								   AbstractEnrichmentService<?, ?> enrichmentService) {
		LOGGER.info("Starting enrichment for '{}' ({}).", mbid, dataType);
		try {
			enrichmentService.executeEnrichment(mbid);
		} catch (RuntimeException e) {
			LOGGER.error("Could not enrich {}' ({}).", mbid, dataType, e);
			return;
		}
		LOGGER.info("Completed enrichment for '{}' ({}).", mbid, dataType);
		historyService.markAsChecked(dataType, mbid);
	}

	@NotNull
	private AbstractEnrichmentService<?, ?> findFittingEnrichmentService(@NotNull DataType dataType) {
		return applicationContext.getBeansOfType(AbstractEnrichmentService.class)
			.values()
			.stream()
			.filter(enrichmentService -> enrichmentService.getDataType() == dataType)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No enrichment service exists for data type '%s'.".formatted(
				dataType)));
	}
}
