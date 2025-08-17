package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import dev.rilling.musicbrainzenricher.core.WorkQueueRepository;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResultRepository;
import dev.rilling.musicbrainzenricher.enrichment.ResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class MusicbrainzEnricherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

	private static final int AUTO_QUERY_CHUNK_SIZE = 50;
	private static final int TAG_SUBMISSION_CHUNK_SIZE = AUTO_QUERY_CHUNK_SIZE;


	private final boolean dryRun;

	private final ApplicationContext applicationContext;
	private final ResultService resultService;
	private final MusicbrainzEditService musicbrainzEditService;
	private final ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository;


	MusicbrainzEnricherService(Environment environment, ApplicationContext applicationContext, ResultService resultService, MusicbrainzEditService musicbrainzEditService, ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository) {
		this.applicationContext = applicationContext;
		this.resultService = resultService;
		this.musicbrainzEditService = musicbrainzEditService;
		this.releaseGroupEnrichmentResultRepository = releaseGroupEnrichmentResultRepository;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
	}


	public void runInAutoQueryMode() {
		executeAutoQueryEnrichment(DataType.RELEASE_GROUP);
		executeAutoQueryEnrichment(DataType.RELEASE);

		submitTags();
	}

	private void executeAutoQueryEnrichment(DataType dataType) {
		final WorkQueueRepository workQueueRepository = findBeanForDataType(dataType, WorkQueueRepository.class);
		final AbstractEnrichmentService<?> enrichmentService = findBeanForDataType(dataType, AbstractEnrichmentService.class);

		LOGGER.info("Starting auto-query for data type {}.", dataType);
		long count = workQueueRepository.countWorkQueue();
		while (count > 0) {
			LOGGER.info("{} auto-query entities remaining.", count);
			for (UUID sourceMbid : workQueueRepository.queryWorkQueue(AUTO_QUERY_CHUNK_SIZE)) {
				executeEnrichment(dataType, sourceMbid, enrichmentService);
			}
			count = workQueueRepository.countWorkQueue();
		}
	}

	public void runInSingleMode(DataType dataType, UUID sourceMbid) {
		executeEnrichment(dataType, sourceMbid, findBeanForDataType(dataType, AbstractEnrichmentService.class));

		submitTags();
	}


	private void executeEnrichment(DataType dataType, UUID sourceMbid, AbstractEnrichmentService<?> enrichmentService) {
		LOGGER.info("Starting enrichment for {} '{}'.", dataType, sourceMbid);
		enrichmentService.executeEnrichment(sourceMbid).ifPresent(results -> {
			resultService.persistResults(dataType, sourceMbid, results);
			LOGGER.info("Completed enrichment for {} '{}'.", dataType, sourceMbid);
		});
	}

	private void submitTags() {
		if (dryRun) {
			return;
		}
		processInChunks(releaseGroupEnrichmentResultRepository.findMergedResults(), TAG_SUBMISSION_CHUNK_SIZE, musicbrainzEditService::submitUserTags);
	}

	private static <T> void processInChunks(Stream<T> stream, int chunkSize, Consumer<Collection<T>> consumer) {
		Iterator<T> iterator = stream.iterator();
		while (iterator.hasNext()) {
			List<T> chunk = new ArrayList<>(chunkSize);
			for (int i = 0; i < chunkSize && iterator.hasNext(); i++) {
				chunk.add(iterator.next());
			}
			consumer.accept(chunk);
		}
	}


	private <T extends DataTypeAware> T findBeanForDataType(DataType dataType, Class<T> clazz) {
		return applicationContext.getBeansOfType(clazz).values().stream().filter(bean -> bean.getDataType() == dataType).findFirst().orElseThrow(() -> new IllegalArgumentException("No bean of type %s exists for data type %s.".formatted(clazz, dataType)));
	}
}
