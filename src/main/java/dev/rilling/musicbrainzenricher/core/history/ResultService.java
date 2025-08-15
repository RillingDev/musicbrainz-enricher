package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.EntityEnrichmentResult;
import dev.rilling.musicbrainzenricher.enrichment.EntityEnrichmentResultRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResultService {
	private final boolean dryRun;

	private final HistoryEntryRepository historyEntryRepository;
	private final EntityEnrichmentResultRepository entityEnrichmentResultRepository;

	ResultService(Environment environment, HistoryEntryRepository historyEntryRepository, EntityEnrichmentResultRepository entityEnrichmentResultRepository) {
		this.historyEntryRepository = historyEntryRepository;
		this.entityEnrichmentResultRepository = entityEnrichmentResultRepository;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
	}

	@Transactional
	public void persistResult(DataType dataType, UUID mbid, EntityEnrichmentResult entityEnrichmentResult) {
		entityEnrichmentResultRepository.persistReleaseGroupResult(entityEnrichmentResult);

		if (!dryRun) {
			historyEntryRepository.persist(new HistoryEntry(dataType, mbid));
		}
	}
}
