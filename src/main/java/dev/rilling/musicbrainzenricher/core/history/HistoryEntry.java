package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;

import java.util.UUID;

record HistoryEntry(DataType dataType, UUID mbid) {
}
