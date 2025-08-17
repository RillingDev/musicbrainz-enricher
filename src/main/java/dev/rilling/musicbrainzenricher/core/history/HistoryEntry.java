package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;

import java.util.UUID;

public record HistoryEntry(DataType dataType, UUID sourceMbid) {
}
