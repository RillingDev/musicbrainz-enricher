package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResult;
import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResultRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ResultService {
	private final boolean dryRun;

	private final HistoryEntryRepository historyEntryRepository;
	private final ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository;

	ResultService(Environment environment, HistoryEntryRepository historyEntryRepository, ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository) {
		this.historyEntryRepository = historyEntryRepository;
		this.releaseGroupEnrichmentResultRepository = releaseGroupEnrichmentResultRepository;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
	}

	@Transactional
	public void persistResults(DataType dataType, UUID mbid, Set<ReleaseGroupEnrichmentResult> results) {
		releaseGroupEnrichmentResultRepository.persistResults(results);

		if (!dryRun) {
			historyEntryRepository.persist(new HistoryEntry(dataType, mbid));
		}
	}
}
