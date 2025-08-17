package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResult;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.query.submission.UserTagSubmissionWs2;
import org.musicbrainz.webservice.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@ThreadSafe
public class MusicbrainzEditService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEditService.class);


	private final boolean dryRun;

	private final WebService webService;
	private final Bucket bucket;
	private final MusicbrainzLookupService musicbrainzLookupService;

	MusicbrainzEditService(Environment environment,
						   @Qualifier("musicbrainzWebService") WebService webService,
						   @Qualifier("musicbrainzBucket") Bucket bucket, MusicbrainzLookupService musicbrainzLookupService) {
		this.webService = webService;
		this.bucket = bucket;

		dryRun = environment.getRequiredProperty("musicbrainz-enricher.dry-run", Boolean.class);
		this.musicbrainzLookupService = musicbrainzLookupService;
	}

	public void submitUserTags(Collection<ReleaseGroupEnrichmentResult> results) {
		if (dryRun) {
			return;
		}

		Map<UUID, Set<String>> resultsByGid =
			results.stream()
				.collect(Collectors.groupingBy(ReleaseGroupEnrichmentResult::gid,
					Collectors.mapping(ReleaseGroupEnrichmentResult::genre, Collectors.toSet())));

		Set<EntityWs2> entities = new HashSet<>(resultsByGid.keySet().size());
		for (Map.Entry<UUID, Set<String>> entry : resultsByGid.entrySet()) {
			UUID gid = entry.getKey();
			Set<String> genres = entry.getValue();

			try {
				LOGGER.info("Fetching entity {}.", gid);
				musicbrainzLookupService.lookUpReleaseGroup(gid, new ReleaseGroupIncludesWs2()).ifPresent(releaseGroup -> {
					addTags(releaseGroup, genres);
					entities.add(releaseGroup);
				});
			} catch (MusicbrainzException e) {
				LOGGER.error("Could not fetch {}.", gid, e);
			}
		}

		doSubmitUserTags(entities);
	}

	private static void addTags(ReleaseGroupWs2 releaseGroup, Set<String> genres) {
		releaseGroup.getUserTags().clear();
		for (String tag : genres) {
			TagWs2 userTag = new TagWs2();
			userTag.setName(tag);
			releaseGroup.addUserTag(userTag);
		}
	}

	private void doSubmitUserTags(Set<EntityWs2> submission) {
		try {
			LOGGER.info("Submitting user tags for {} entities.", submission.size());
			submitUserTags(submission);
			LOGGER.info("Successfully submitted user tags for {} entities.", submission.size());
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not submit user tags.", e);
		}
	}

	/**
	 * Submits the given tags set in the given entities.
	 *
	 * @param entities Entities with tags set.
	 * @throws MusicbrainzException If API access fails.
	 */
	private void submitUserTags(Set<EntityWs2> entities) throws MusicbrainzException {
		bucket.asBlocking().consumeUninterruptibly(1);

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
