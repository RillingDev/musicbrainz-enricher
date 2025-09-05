package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.history.HistoryEntry;
import dev.rilling.musicbrainzenricher.core.history.HistoryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ResultService {
	private final HistoryEntryRepository historyEntryRepository;
	private final ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository;

	ResultService(HistoryEntryRepository historyEntryRepository, ReleaseGroupEnrichmentResultRepository releaseGroupEnrichmentResultRepository) {
		this.historyEntryRepository = historyEntryRepository;
		this.releaseGroupEnrichmentResultRepository = releaseGroupEnrichmentResultRepository;
	}

	@Transactional
	public void persistResults(DataType dataType, UUID sourceMbid, Set<ReleaseGroupEnrichmentResult> results) {
		releaseGroupEnrichmentResultRepository.persistResults(results);
		historyEntryRepository.persist(new HistoryEntry(dataType, sourceMbid));
	}
}
