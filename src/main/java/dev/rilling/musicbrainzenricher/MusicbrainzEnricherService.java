package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import dev.rilling.musicbrainzenricher.core.WorkQueueRepository;
import dev.rilling.musicbrainzenricher.core.history.HistoryService;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.EntityEnrichmentResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MusicbrainzEnricherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

	private static final int AUTO_QUERY_CHUNK_SIZE = 100;
	private static final double MIN_GENRE_USAGE = 0.90;

	private final ApplicationContext applicationContext;
	private final HistoryService historyService;
	private final MusicbrainzEditController musicbrainzEditController;
	private final EntityEnrichmentResultRepository entityEnrichmentResultRepository;


	MusicbrainzEnricherService(ApplicationContext applicationContext, HistoryService historyService, MusicbrainzEditController musicbrainzEditController, EntityEnrichmentResultRepository entityEnrichmentResultRepository) {
		this.applicationContext = applicationContext;
		this.historyService = historyService;
		this.musicbrainzEditController = musicbrainzEditController;
		this.entityEnrichmentResultRepository = entityEnrichmentResultRepository;
	}

	public void runInAutoQueryMode(DataType dataType) {
		final WorkQueueRepository workQueueRepository = findBeanForDataType(dataType, WorkQueueRepository.class);
		final AbstractEnrichmentService<?> enrichmentService = findBeanForDataType(dataType, AbstractEnrichmentService.class);

		long count = workQueueRepository.countWorkQueue();
		while (count > 0) {
			LOGGER.info("{} auto-query entities remaining.", count);
			for (UUID mbid : workQueueRepository.queryWorkQueue(AUTO_QUERY_CHUNK_SIZE)) {
				executeEnrichment(dataType, mbid, enrichmentService);
			}
			count = workQueueRepository.countWorkQueue();
		}

		submitTags();
	}

	public void runInSingleMode(DataType dataType, UUID mbid) {
		executeEnrichment(dataType, mbid, findBeanForDataType(dataType, AbstractEnrichmentService.class));
		submitTags();
	}

	private void executeEnrichment(DataType dataType, UUID mbid, AbstractEnrichmentService<?> enrichmentService) {
		LOGGER.info("Starting enrichment for {} '{}'.", dataType, mbid);
		enrichmentService.executeEnrichment(mbid).ifPresent(entityEnrichmentResult -> {
			// TODO run in transaction
			entityEnrichmentResultRepository.persistReleaseGroupResult(entityEnrichmentResult);
			historyService.markAsChecked(dataType, mbid);
			LOGGER.info("Completed enrichment for {} '{}'.", dataType, mbid);
		});
	}

	private void submitTags() {
		// TODO load from merged view
		//musicbrainzEditController.submitReleaseGroupUserTags(enrichmentProcessResult.targetEntity(), newGenres);
	}


	private <T extends DataTypeAware> T findBeanForDataType(DataType dataType, Class<T> clazz) {
		return applicationContext.getBeansOfType(clazz).values().stream().filter(bean -> bean.getDataType() == dataType).findFirst().orElseThrow(() -> new IllegalArgumentException("No bean of type %s exists for data type %s.".formatted(clazz, dataType)));
	}
}
