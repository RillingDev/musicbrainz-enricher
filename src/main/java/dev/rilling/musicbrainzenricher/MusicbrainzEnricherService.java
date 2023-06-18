package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import dev.rilling.musicbrainzenricher.core.WorkQueueRepository;
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

	private static final int AUTO_QUERY_CHUNK_SIZE = 100;

	private final ApplicationContext applicationContext;
	private final HistoryService historyService;

	MusicbrainzEnricherService(ApplicationContext applicationContext, HistoryService historyService) {
		this.applicationContext = applicationContext;
		this.historyService = historyService;
	}

	public void runInAutoQueryMode(@NotNull DataType dataType) {
		final WorkQueueRepository workQueueRepository = findBeanForDataType(dataType, WorkQueueRepository.class);
		final AbstractEnrichmentService<?, ?> enrichmentService = findBeanForDataType(dataType, AbstractEnrichmentService.class);

		long count = workQueueRepository.countFromWorkQueue();
		while (count > 0) {
			LOGGER.info("{} auto-query entities remaining.", count);
			for (UUID mbid : workQueueRepository.findFromWorkQueue(AUTO_QUERY_CHUNK_SIZE)) {
				executeEnrichment(dataType, mbid, enrichmentService);
			}
			count = workQueueRepository.countFromWorkQueue();
		}
	}

	public void runInSingleMode(@NotNull DataType dataType, @NotNull UUID mbid) {
		executeEnrichment(dataType, mbid, findBeanForDataType(dataType, AbstractEnrichmentService.class));
	}

	private void executeEnrichment(@NotNull DataType dataType, @NotNull UUID mbid, AbstractEnrichmentService<?, ?> enrichmentService) {
		LOGGER.info("Starting enrichment for {} '{}'.", dataType, mbid);
		try {
			enrichmentService.executeEnrichment(mbid);
		} catch (RuntimeException e) {
			LOGGER.error("Could not enrich {} '{}'.", dataType, mbid, e);
			return;
		}
		// TODO: should not persist if submission was not flushed yet.
		LOGGER.info("Completed enrichment for {} '{}'.", dataType, mbid);
		historyService.markAsChecked(dataType, mbid);
	}

	@NotNull
	private <T extends DataTypeAware> T findBeanForDataType(@NotNull DataType dataType, Class<T> clazz) {
		return applicationContext.getBeansOfType(clazz).values().stream().filter(bean -> bean.getDataType() == dataType).findFirst().orElseThrow(() -> new IllegalArgumentException("No bean of type %s exists for data type %s.".formatted(clazz, dataType)));
	}
}
