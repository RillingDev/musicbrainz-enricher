package org.felixrilling.musicbrainzenricher.core.history;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    private static final Duration RECHECK_TIMESPAN = Duration.ofDays(90);
    private static final ZoneOffset ZONE = ZoneOffset.UTC;

    private final HistoryEntryRepository historyEntryRepository;

    HistoryService(HistoryEntryRepository historyEntryRepository) {
        this.historyEntryRepository = historyEntryRepository;
    }

    public boolean checkIsDue(@NotNull DataType dataType, @NotNull UUID mbid) {
        logger.trace("Checking history entry for '{}' ({}).", mbid, dataType);
        return historyEntryRepository.findEntryByTypeAndMbid(dataType, mbid)
                .map(this::checkIsDue).orElse(true);
    }

    private boolean checkIsDue(@NotNull HistoryEntry historyEntry) {
        return historyEntry.getLastChecked().isBefore(ZonedDateTime.now(ZONE).minus(RECHECK_TIMESPAN));
    }

    public void markAsChecked(@NotNull DataType dataType, @NotNull UUID mbid) {
        HistoryEntry historyEntry = historyEntryRepository.findEntryByTypeAndMbid(dataType, mbid).orElseGet(() -> {
            HistoryEntry entry = new HistoryEntry();
            entry.setDataType(dataType);
            entry.setMbid(mbid);
            return entry;
        });
        historyEntry.setLastChecked(ZonedDateTime.now(ZONE));
        logger.trace("Persisting history entry: '{}'.", historyEntry);
        historyEntryRepository.save(historyEntry);
    }
}
