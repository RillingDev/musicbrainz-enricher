package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

record HistoryEntry(@NotNull DataType dataType, @NotNull UUID mbid) {
}
