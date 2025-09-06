package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import dev.rilling.musicbrainzenricher.core.WorkQueueRepository;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResult;
import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResultRepository;
import dev.rilling.musicbrainzenricher.enrichment.ResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MusicbrainzEnricherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherService.class);

	private static final int AUTO_QUERY_CHUNK_SIZE = 50;
	private static final int TAG_SUBMISSION_CHUNK_SIZE = AUTO_QUERY_CHUNK_SIZE;

	private final ApplicationContext applicationContext;
	private final ResultService resultService;
	private final MusicbrainzEditService musicbrainzEditService;
	private final ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository;

	MusicbrainzEnricherService(ApplicationContext applicationContext, ResultService resultService, MusicbrainzEditService musicbrainzEditService, ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository) {
		this.applicationContext = applicationContext;
		this.resultService = resultService;
		this.musicbrainzEditService = musicbrainzEditService;
		this.releaseGroupEnrichmentResultRepository = releaseGroupEnrichmentResultRepository;
	}


	public void gather(DataType dataType, UUID sourceMbid) {
		doGather(dataType, sourceMbid, findBeanForDataType(dataType, AbstractEnrichmentService.class));
	}

	public void gatherAll() {
		gatherForType(DataType.RELEASE_GROUP);
		gatherForType(DataType.RELEASE);
	}

	private void gatherForType(DataType dataType) {
		final WorkQueueRepository workQueueRepository = findBeanForDataType(dataType, WorkQueueRepository.class);
		final AbstractEnrichmentService<?> enrichmentService = findBeanForDataType(dataType, AbstractEnrichmentService.class);

		long count = workQueueRepository.countWorkQueue();
		while (count > 0) {
			LOGGER.info("{} {} entities remaining.", count, dataType);
			for (UUID sourceMbid : workQueueRepository.queryWorkQueue(AUTO_QUERY_CHUNK_SIZE)) {
				doGather(dataType, sourceMbid, enrichmentService);
			}
			count = workQueueRepository.countWorkQueue();
		}
	}

	private void doGather(DataType dataType, UUID sourceMbid, AbstractEnrichmentService<?> enrichmentService) {
		LOGGER.info("Starting to gather data for {} '{}'.", dataType, sourceMbid);
		enrichmentService.executeEnrichment(sourceMbid).ifPresent(results -> {
			resultService.persistResults(dataType, sourceMbid, results);
			LOGGER.info("Completed gathering data for {} '{}'.", dataType, sourceMbid);
		});
	}

	public void submitTags() {
		int offset = 0;
		List<ReleaseGroupEnrichmentResult> results;
		do {
			results = releaseGroupEnrichmentResultRepository.findMergedResults(TAG_SUBMISSION_CHUNK_SIZE, offset);
			LOGGER.info("Submitting data for {} results.", results.size());
			musicbrainzEditService.submitUserTags(results);
			offset += results.size();
		} while (!results.isEmpty());
	}

	private <T extends DataTypeAware> T findBeanForDataType(DataType dataType, Class<T> clazz) {
		return applicationContext.getBeansOfType(clazz).values().stream().filter(bean -> bean.getDataType() == dataType).findFirst().orElseThrow(() -> new IllegalArgumentException("No bean of type %s exists for data type %s.".formatted(clazz, dataType)));
	}
}
