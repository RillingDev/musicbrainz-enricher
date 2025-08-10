package dev.rilling.musicbrainzenricher.api.musicbrainz;

import jakarta.annotation.PreDestroy;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

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
	public void submitReleaseGroupUserTags(ReleaseGroupWs2 releaseGroup, Set<String> tags) {
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

	private void doSubmitUserTags(Set<EntityWs2> submission) {
		try {
			LOGGER.info("Submitting user tags for {} entities.", submission.size());
			musicbrainzEditService.submitUserTags(submission);
			LOGGER.info("Successfully submitted user tags for {} entities.", submission.size());
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not submit user tags.", e);
		}
	}

	private void addTags(ReleaseGroupWs2 releaseGroup, Set<String> tags) {
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
