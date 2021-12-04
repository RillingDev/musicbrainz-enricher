package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;
import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.query.submission.UserTagSubmissionWs2;
import org.musicbrainz.webservice.WebService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@ThreadSafe
public class MusicbrainzEditService {

	private final boolean dryRun;

	private final WebService webService;
	private final MusicbrainzBucketProvider musicbrainzBucketProvider;
	private final BucketService bucketService;

	MusicbrainzEditService(Environment environment,
						   @Qualifier("musicbrainzWebService") WebService webService,
						   MusicbrainzBucketProvider musicbrainzBucketProvider,
						   BucketService bucketService) {
		this.webService = webService;
		this.musicbrainzBucketProvider = musicbrainzBucketProvider;
		this.bucketService = bucketService;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
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
		if (dryRun) {
			return;
		}

		bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

		/*
		 * This is a modified version of Controller#AddTags, adapted to avoid re-querying entities we already have.
		 */
		releaseGroup.getUserTags().clear();
		for (String tag : tags) {
			TagWs2 userTag = new TagWs2();
			userTag.setName(tag);
			releaseGroup.addUserTag(userTag);
		}
		UserTagSubmissionWs2 query = new UserTagSubmissionWs2(webService);
		try {
			query.addEntity(releaseGroup);
			query.submit();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not submit tags.", e);
		}
	}

}
