package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class HistoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryService.class);

	private static final Duration RECHECK_TIMESPAN = Duration.ofDays(90);
	private static final ZoneOffset ZONE = ZoneOffset.UTC;

	private final boolean dryRun;

	private final HistoryEntryRepository historyEntryRepository;

	HistoryService(Environment environment, HistoryEntryRepository historyEntryRepository) {
		this.historyEntryRepository = historyEntryRepository;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
	}

	public boolean checkIsDue(@NotNull DataType dataType, @NotNull UUID mbid) {
		LOGGER.trace("Checking history entry for '{}' ({}).", mbid, dataType);
		ZonedDateTime consideredActiveIfAfter = getNow().minus(RECHECK_TIMESPAN);
		return !historyEntryRepository.hasActiveEntry(dataType, mbid, consideredActiveIfAfter);
	}

	public void markAsChecked(@NotNull DataType dataType, @NotNull UUID mbid) {
		if (dryRun) {
			return;
		}

		HistoryEntry historyEntry = new HistoryEntry(dataType, mbid, getNow());
		LOGGER.trace("Persisting history entry: '{}'.", historyEntry);
		historyEntryRepository.persist(historyEntry);
	}

	@NotNull
	private ZonedDateTime getNow() {
		return ZonedDateTime.now(ZONE);
	}
}
