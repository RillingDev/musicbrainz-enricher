package dev.rilling.musicbrainzenricher.core;

import java.util.List;
import java.util.UUID;

public interface WorkQueueRepository extends DataTypeAware {
	long countWorkQueue();

	 List<UUID> queryWorkQueue(int limit);
}
