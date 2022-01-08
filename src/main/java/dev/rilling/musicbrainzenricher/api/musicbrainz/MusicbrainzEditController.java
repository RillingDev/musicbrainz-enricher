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

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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

	private static final int TAG_SUBMISSION_SIZE = 50;

	private final MusicbrainzEditService musicbrainzEditService;
	private final ExecutorService executorService;

	private final Queue<EntityWs2> tagSubmissionQueue = new ConcurrentLinkedQueue<>();

	private final Object tagSubmissionLock = new Object();

	public MusicbrainzEditController(MusicbrainzEditService musicbrainzEditService,
									 @Qualifier("submissionExecutor") ExecutorService executorService) {
		this.musicbrainzEditService = musicbrainzEditService;
		this.executorService = executorService;
	}

	/**
	 * Submits the given tags as tags for the entity.
	 *
	 * @param releaseGroup Entity to add tags to. Note that this is mutated to contain the new tags. Note that this
	 *                     object may not be changed by the caller afterwards anymore.
	 * @param tags         Tags to add.
	 * @return Future for completion.
	 * @throws MusicbrainzException If API access fails.
	 */
	public @NotNull Future<?> submitReleaseGroupUserTags(@NotNull ReleaseGroupWs2 releaseGroup,
														 @NotNull Set<String> tags) throws MusicbrainzException {
		addTags(releaseGroup, tags);
		tagSubmissionQueue.add(releaseGroup);

		// Check-then-act is not synchronized as #flushUserTagSubmission handles multiple concurrent invocations.
		if (tagSubmissionQueue.size() >= TAG_SUBMISSION_SIZE) {
			LOGGER.debug("{} user tag submissions exceeded, flushing.", TAG_SUBMISSION_SIZE);
			return flushUserTagSubmission();
		}

		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Flushes any pending changes.
	 *
	 * @return Future for completion.
	 */
	public @NotNull Future<?> flush() {
		return flushUserTagSubmission();
	}

	private @NotNull Future<?> flushUserTagSubmission() {
		LOGGER.info("Flushing tag submission.");

		Set<EntityWs2> submission;
		synchronized (tagSubmissionLock) {
			if (tagSubmissionQueue.isEmpty()) {
				LOGGER.info("Queue is empty, no user tags to submit.");
				return CompletableFuture.completedFuture(null);
			}

			// Note that queue may be larger than #TAG_SUBMISSION_SIZE at this point as the queue can still be modified.
			LOGGER.debug("Scheduling {} items for user tag submission.", tagSubmissionQueue.size());
			submission = Set.copyOf(tagSubmissionQueue);
			// Used over #clear as queue itself may have been modified since we copied its values
			tagSubmissionQueue.removeAll(submission);
		}

		return executorService.submit(() -> {
			try {
				LOGGER.info("Submitting user tags for '{}'.", submission);
				musicbrainzEditService.submitUserTags(submission);
				LOGGER.info("Successfully submitted user tags for '{}'.", submission);
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
}
