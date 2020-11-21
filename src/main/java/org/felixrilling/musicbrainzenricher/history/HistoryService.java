package org.felixrilling.musicbrainzenricher.history;

import org.felixrilling.musicbrainzenricher.DataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class HistoryService {
    private static final Duration RECHECK_DURATION = Duration.ofDays(7);
    private static final ZoneOffset ZONE = ZoneOffset.UTC;

    private final HistoryEntryRepository historyEntryRepository;

    HistoryService(HistoryEntryRepository historyEntryRepository) {
        this.historyEntryRepository = historyEntryRepository;
    }

    public boolean checkIsDue(@NotNull DataType dataType, @NotNull String mbid) {
        return historyEntryRepository.findFirstByDataTypeAndMbid(dataType, mbid)
                .map(this::recheckIsDue).orElse(true);
    }

    public void markAsChecked(@NotNull DataType dataType, @NotNull String mbid) {
        HistoryEntry historyEntry = historyEntryRepository.findFirstByDataTypeAndMbid(dataType, mbid).orElseGet(() -> {
            HistoryEntry entry = new HistoryEntry();
            entry.setDataType(dataType);
            entry.setMbid(mbid);
            return entry;
        });
        historyEntry.setLastChecked(ZonedDateTime.now(ZONE));
        historyEntryRepository.save(historyEntry);
    }

    private boolean recheckIsDue(@NotNull  HistoryEntry historyEntry) {
        return historyEntry.getLastChecked().isBefore(ZonedDateTime.now(ZONE).minus(RECHECK_DURATION));
    }
}
