package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MusicbrainzEditController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEditController.class);

	private final MusicbrainzEditService musicbrainzEditService;

	private final Queue<EntityWs2> tagSubmissionQueue = new ConcurrentLinkedQueue<>();

	private final Object tagSubmissionLock = new Object();
	private static final int SUBMISSION_SIZE = 10;

	public MusicbrainzEditController(MusicbrainzEditService musicbrainzEditService) {
		this.musicbrainzEditService = musicbrainzEditService;
	}

	/**
	 * Submits the given tags as tags for the entity.
	 *
	 * @param releaseGroup Entity to add tags to. Note that this is mutated to contain the new tags.
	 * @param tags         Tags to add.
	 * @throws MusicbrainzException If API access fails.
	 */
	public void submitReleaseGroupUserTags(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull Set<String> tags)
		throws MusicbrainzException {

		addTags(releaseGroup, tags);
		tagSubmissionQueue.add(releaseGroup);

		synchronized (tagSubmissionLock) {
			if (tagSubmissionQueue.size() >= SUBMISSION_SIZE) {
				flushUserTagSubmission();
			}
		}

	}

	public void flush() throws MusicbrainzException {
		flushUserTagSubmission();
	}

	private void flushUserTagSubmission() throws MusicbrainzException {
		LOGGER.info("Flushing tag submission.");
		Set<EntityWs2> submission = new HashSet<>(SUBMISSION_SIZE);
		int i = 0;
		while (tagSubmissionQueue.peek() != null && i < SUBMISSION_SIZE) {
			submission.add(tagSubmissionQueue.poll());
			i++;
		}
		musicbrainzEditService.submitUserTags(submission);
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
