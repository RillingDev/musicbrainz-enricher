package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HistoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

	private final boolean dryRun;

	private final HistoryEntryRepository historyEntryRepository;

	HistoryService(Environment environment, HistoryEntryRepository historyEntryRepository) {
		this.historyEntryRepository = historyEntryRepository;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
	}

	public void markAsChecked( DataType dataType,  UUID mbid) {
		if (dryRun) {
			return;
		}

		HistoryEntry historyEntry = new HistoryEntry(dataType, mbid);
		LOGGER.trace("Persisting history entry: '{}'.", historyEntry);
		historyEntryRepository.persist(historyEntry);
	}
}
