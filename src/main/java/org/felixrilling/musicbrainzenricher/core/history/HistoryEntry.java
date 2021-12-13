package org.felixrilling.musicbrainzenricher.core.history;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

record HistoryEntry(@NotNull DataType dataType, @NotNull UUID mbid, @NotNull ZonedDateTime lastChecked) {
}
