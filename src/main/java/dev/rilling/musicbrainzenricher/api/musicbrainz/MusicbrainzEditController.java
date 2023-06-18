package dev.rilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Manages edits against the musicbrainz API.
 * <p>
 * This controller also attempts to reduce requests by grouping data submissions. During application shutdown,
 * {@link #flush()} should be called to ensure all remaining data is submitted.
 */
@Service
@ThreadSafe
public class MusicbrainzEditController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEditController.class);

	static final int TAG_SUBMISSION_SIZE = 50;

	private final MusicbrainzEditService musicbrainzEditService;
	private final ExecutorService executorService;

	private final ChunkedWorker<EntityWs2, Future<?>> tagSubmissionWorker;

	public MusicbrainzEditController(MusicbrainzEditService musicbrainzEditService,
									 @Qualifier("submissionExecutor") ExecutorService executorService) {
		this.musicbrainzEditService = musicbrainzEditService;
		this.executorService = executorService;

		tagSubmissionWorker = new ChunkedWorker<>(TAG_SUBMISSION_SIZE, this::doSubmitUserTags);
	}

	/**
	 * Submits the given tags as tags for the entity.
	 *
	 * @param releaseGroup Entity to add tags to. Note that this is mutated to contain the new tags. Note that this
	 *                     object may not be changed by the caller afterwards anymore.
	 * @param tags         Tags to add.
	 * @return Future for completion.
	 */
	public @NotNull Future<?> submitReleaseGroupUserTags(@NotNull ReleaseGroupWs2 releaseGroup,
														 @NotNull Set<String> tags) {
		addTags(releaseGroup, tags);
		return tagSubmissionWorker.add(releaseGroup).orElse(CompletableFuture.completedFuture(null));
	}

	/**
	 * Flushes any pending changes.
	 *
	 * @return Future for completion.
	 */
	public @NotNull Future<?> flush() {
		return tagSubmissionWorker.flush().orElse(CompletableFuture.completedFuture(null));
	}

	@NotNull
	private Future<?> doSubmitUserTags(@NotNull Set<EntityWs2> submission) {
		LOGGER.debug("Scheduling user tags for submission.");
		return executorService.submit(() -> {
			try {
				LOGGER.info("Submitting user tags for {} entities.", submission.size());
				musicbrainzEditService.submitUserTags(submission);
				LOGGER.info("Successfully submitted user tags for {} entities.", submission.size());
			} catch (MusicbrainzException e) {
				LOGGER.error("Could not submit user tags.", e);
			}
		});
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
	 * Chunks may have less than N items (but at least 1) if processing was forced using {@link #flush()}.
	 *
	 * @param <TItem>   Item type.
	 * @param <UResult> Result type.
	 */
	@ThreadSafe
	private static class ChunkedWorker<TItem, UResult> {
		private final int chunkSize;

		private final Queue<TItem> queue = new ConcurrentLinkedQueue<>();

		private final Object workChunkingLock = new Object();

		private final Function<Set<TItem>, UResult> workProcessor;

		/**
		 * @param chunkSize     Size of a chunk. Once reached, automatic flushing is done.
		 * @param workProcessor Function processing a chunk of items. The chunk of items will not be modified by this
		 *                      worker after calling the processor.
		 */
		ChunkedWorker(int chunkSize, @NotNull Function<Set<TItem>, UResult> workProcessor) {
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
		 * @return {@link #workProcessor} result, or empty not enough items exist yet for processing.
		 */
		public @NotNull Optional<UResult> add(TItem input) {
			queue.add(input);
			return flush(false);
		}

		/**
		 * Flushes incomplete chunks. If any number of items are present, this may lead to an invocation of
		 * {@link #workProcessor}.
		 *
		 * @return {@link #workProcessor} result, or empty no items exist for processing.
		 */
		public @NotNull Optional<UResult> flush() {
			return flush(true);
		}

		private @NotNull Optional<UResult> flush(boolean force) {
			Set<TItem> chunk;
			synchronized (workChunkingLock) {
				if (force) {
					if (queue.isEmpty()) {
						return Optional.empty();
					}
				} else {
					if (queue.size() < chunkSize) {
						return Optional.empty();
					}
				}

				final Set<TItem> items = new HashSet<>(chunkSize);
				// Note that queue may be larger than chunkSize at this point as the queue can still be modified.
				// Due to that, we only take chunkSize items.
				while (queue.peek() != null && items.size() < chunkSize) {
					items.add(queue.poll());
				}

				chunk = Collections.unmodifiableSet(items);
			}

			return Optional.of(workProcessor.apply(chunk));
		}
	}
}
