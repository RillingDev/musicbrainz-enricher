package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;
import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.model.entity.EntityWs2;
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
	 * Submits the given tags set in the given entities.
	 * Usually this should not be called directly and {@link MusicbrainzEditController} should be used instead.
	 *
	 * @param entities Entities with tags set.
	 * @throws MusicbrainzException If API access fails.
	 */
	public void submitUserTags(@NotNull Set<EntityWs2> entities) throws MusicbrainzException {
		if (dryRun) {
			return;
		}

		bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

		UserTagSubmissionWs2 query = new UserTagSubmissionWs2(webService);
		try {
			for (EntityWs2 entity : entities) {
				query.addEntity(entity);
			}
			query.submit();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not submit tags.", e);
		}
	}

}
