package dev.rilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Worker processing chunks of up to N items.
 * If after addition at least N items are present, they are immediately processed as a chunk of N
 * Chunks may have fewer than N items (but at least 1) if processing was forced using {@link #flush()}.
 *
 * @param <TItem> Item type.
 */
@ThreadSafe
class ChunkedWorker<TItem> {
	private final int chunkSize;

	private final Queue<TItem> queue = new ConcurrentLinkedQueue<>();

	private final Object workChunkingLock = new Object();

	private final Consumer<Set<TItem>> workProcessor;

	/**
	 * @param chunkSize     Size of a chunk. Once reached, automatic flushing is done.
	 * @param workProcessor Function processing a chunk of items. The chunk of items will not be modified by this
	 *                      worker after calling the processor.
	 */
	ChunkedWorker(int chunkSize, Consumer<Set<TItem>> workProcessor) {
		if (chunkSize < 1) {
			throw new IllegalArgumentException("Chunk size must be at least 1.");
		}
		this.chunkSize = chunkSize;
		this.workProcessor = workProcessor;
	}

	/**
	 * Adds an item. If chunk size is reached, this may lead to an invocation of {@link #workProcessor}.
	 *
	 * @param input Item to add.
	 */
	public void add(TItem input) {
		queue.add(input);
		flush(false);
	}

	/**
	 * Flushes incomplete chunks. If any items are present, this may lead to an invocation of
	 * {@link #workProcessor}.
	 */
	public void flush() {
		flush(true);
	}

	private void flush(boolean force) {
		final Set<TItem> items;
		synchronized (workChunkingLock) {
			if (force) {
				if (queue.isEmpty()) {
					return;
				}
			} else {
				if (queue.size() < chunkSize) {
					return;
				}
			}

			// Note that the queue may be larger than chunkSize at this point as the queue can still be modified.
			// Due to that, we only take chunkSize items.
			items = new HashSet<>(chunkSize);
			while (queue.peek() != null && items.size() < chunkSize) {
				items.add(queue.poll());
			}
		}

		workProcessor.accept(Collections.unmodifiableSet(items));
	}
}
