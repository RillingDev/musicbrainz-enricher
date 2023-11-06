package dev.rilling.musicbrainzenricher.api.musicbrainz;

import jakarta.annotation.PreDestroy;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Manages edits against the musicbrainz API.
 * <p>
 * This controller also attempts to reduce requests by grouping data submissions. During application shutdown,
 * {@link #flush()} is called to ensure all remaining data is submitted.
 */
@Service
@ThreadSafe
public class MusicbrainzEditController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEditController.class);

	static final int TAG_SUBMISSION_SIZE = 50;

	private final MusicbrainzEditService musicbrainzEditService;

	private final ChunkedWorker<EntityWs2> tagSubmissionWorker;

	public MusicbrainzEditController(MusicbrainzEditService musicbrainzEditService) {
		this.musicbrainzEditService = musicbrainzEditService;
		tagSubmissionWorker = new ChunkedWorker<>(TAG_SUBMISSION_SIZE, this::doSubmitUserTags);
	}

	/**
	 * Submits the given tags as tags for the entity.
	 *
	 * @param releaseGroup Entity to add tags to. Note that this is mutated to contain the new tags. Note that this
	 *                     object may not be changed by the caller afterward.
	 * @param tags         Tags to add.
	 */
	public void submitReleaseGroupUserTags(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull Set<String> tags) {
		addTags(releaseGroup, tags);
		tagSubmissionWorker.add(releaseGroup);
	}

	/**
	 * Flushes any pending changes.
	 */
	@PreDestroy
	public void flush() {
		LOGGER.debug("Flushing tag submission worker.");
		tagSubmissionWorker.flush();
		LOGGER.debug("Flushed pending edits.");
	}

	private void doSubmitUserTags(@NotNull Set<EntityWs2> submission) {
		try {
			LOGGER.info("Submitting user tags for {} entities.", submission.size());
			musicbrainzEditService.submitUserTags(submission);
			LOGGER.info("Successfully submitted user tags for {} entities.", submission.size());
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not submit user tags.", e);
		}
	}

	private void addTags(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull Set<String> tags) {
		/*
		 * This is a modified version of Controller#AddTags, adapted to avoid re-querying entities we already have.
		 */
		releaseGroup.getUserTags().clear();
		for (String tag : tags) {
			TagWs2 userTag = new TagWs2();
			userTag.setName(tag);
			releaseGroup.addUserTag(userTag);
		}
	}

	/**
	 * Worker processing chunks of up to N items.
	 * If after addition at least N items are present, they are immediately processed as a chunk of N
	 * Chunks may have fewer than N items (but at least 1) if processing was forced using {@link #flush()}.
	 *
	 * @param <TItem> Item type.
	 */
	@ThreadSafe
	private static class ChunkedWorker<TItem> {
		private final int chunkSize;

		private final Queue<TItem> queue = new ConcurrentLinkedQueue<>();

		private final Object workChunkingLock = new Object();

		private final @NotNull Consumer<Set<TItem>> workProcessor;

		/**
		 * @param chunkSize     Size of a chunk. Once reached, automatic flushing is done.
		 * @param workProcessor Function processing a chunk of items. The chunk of items will not be modified by this
		 *                      worker after calling the processor.
		 */
		ChunkedWorker(int chunkSize, @NotNull Consumer<Set<TItem>> workProcessor) {
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
			Set<TItem> chunk;
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

				final Set<TItem> items = new HashSet<>(chunkSize);
				// Note that the queue may be larger than chunkSize at this point as the queue can still be modified.
				// Due to that, we only take chunkSize items.
				while (queue.peek() != null && items.size() < chunkSize) {
					items.add(queue.poll());
				}

				chunk = Collections.unmodifiableSet(items);
			}

			workProcessor.accept(chunk);
		}
	}
}
