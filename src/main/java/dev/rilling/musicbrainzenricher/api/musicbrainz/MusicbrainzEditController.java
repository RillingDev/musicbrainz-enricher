package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.enrichment.ReleaseGroupEnrichmentResult;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages edits against the musicbrainz API.
 */
@Service
@ThreadSafe
public class MusicbrainzEditController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEditController.class);


	private final MusicbrainzLookupService musicbrainzLookupService;
	private final MusicbrainzEditService musicbrainzEditService;

	public MusicbrainzEditController(MusicbrainzLookupService musicbrainzLookupService, MusicbrainzEditService musicbrainzEditService) {
		this.musicbrainzLookupService = musicbrainzLookupService;
		this.musicbrainzEditService = musicbrainzEditService;
	}

	// TODO cleanup
	public void submitReleaseGroupUserTags(Collection<ReleaseGroupEnrichmentResult> chunk) {
		Map<UUID, Set<String>> byGid =
			chunk.stream()
				.collect(Collectors.groupingBy(ReleaseGroupEnrichmentResult::gid,
					Collectors.mapping(ReleaseGroupEnrichmentResult::genre, Collectors.toSet())));

		Set<EntityWs2> enrichedEntities = new HashSet<>(byGid.keySet().size());
		for (Map.Entry<UUID, Set<String>> entry : byGid.entrySet()) {
			UUID gid = entry.getKey();
			Set<String> genres = entry.getValue();

			try {
				LOGGER.info("Fetching entity {}.", gid);
				musicbrainzLookupService.lookUpReleaseGroup(gid, new ReleaseGroupIncludesWs2()).ifPresent(releaseGroup -> {
					setTags(releaseGroup, genres);
					enrichedEntities.add(releaseGroup);
				});
			} catch (MusicbrainzException e) {
				LOGGER.error("Could not fetch {}.", gid, e);
			}
		}

		doSubmitUserTags(enrichedEntities);
	}

	private static void setTags(ReleaseGroupWs2 releaseGroup, Set<String> genres) {
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
			musicbrainzEditService.submitUserTags(submission);
			LOGGER.info("Successfully submitted user tags for {} entities.", submission.size());
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not submit user tags.", e);
		}
	}
}
