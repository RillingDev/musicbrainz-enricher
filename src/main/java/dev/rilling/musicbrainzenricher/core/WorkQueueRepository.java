package dev.rilling.musicbrainzenricher.core;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface WorkQueueRepository extends DataTypeAware {
	long countWorkQueue();

	@NotNull List<UUID> queryWorkQueue(int limit);
}
