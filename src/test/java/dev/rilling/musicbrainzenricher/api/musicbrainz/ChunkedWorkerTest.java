package dev.rilling.musicbrainzenricher.api.musicbrainz;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkedWorkerTest {

	@Test
	@DisplayName("works on chunks.")
	void worksOnChunk() {
		Set<Integer> results = new HashSet<>();

		ChunkedWorker<Integer> chunkedWorker = new ChunkedWorker<>(5, results::addAll);

		chunkedWorker.add(1);
		chunkedWorker.add(2);
		chunkedWorker.add(3);
		chunkedWorker.add(4);
		assertThat(results).isEmpty();
		chunkedWorker.add(5);
		assertThat(results).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
	}

	@Test
	@DisplayName("flushes on demand.")
	void flushes() {
		Set<Integer> results = new HashSet<>();

		ChunkedWorker<Integer> chunkedWorker = new ChunkedWorker<>(5, results::addAll);

		chunkedWorker.add(1);
		chunkedWorker.add(2);
		chunkedWorker.add(3);
		chunkedWorker.add(4);
		chunkedWorker.flush();
		assertThat(results).containsExactlyInAnyOrder(1, 2, 3, 4);
	}

	@Test
	@DisplayName("skips flushing if no items exist")
	void skipsFlushing() {
		AtomicBoolean called = new AtomicBoolean(false);

		ChunkedWorker<Integer> chunkedWorker = new ChunkedWorker<>(5, ignored -> called.set(true));

		chunkedWorker.flush();
		assertThat(called).isFalse();
	}
}
