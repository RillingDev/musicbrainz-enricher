package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.core.ReleaseGroupRepository;
import dev.rilling.musicbrainzenricher.core.ReleaseRepository;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@ThreadSafe
public class MusicbrainzAutoQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzAutoQueryService.class);

	private static final Duration RECHECK_TIMESPAN = Duration.ofDays(90);

	private static final int LIMIT = 1000;

	private final ReleaseRepository releaseRepository;
	private final ReleaseGroupRepository releaseGroupRepository;


	MusicbrainzAutoQueryService(ReleaseRepository releaseRepository, ReleaseGroupRepository releaseGroupRepository) {
		this.releaseRepository = releaseRepository;
		this.releaseGroupRepository = releaseGroupRepository;
	}

	public void autoQueryReleasesWithRelationships(@NotNull Consumer<UUID> mbidConsumer) {
		long count = releaseRepository.countNewReleasesWhereRelationshipsExist();
		LOGGER.info("Found a total of {} new auto query releases.", count);

		long offset = 0;
		while (offset < count) {
			LOGGER.info("Loading {} releases with offset {}...", LIMIT, offset);
			releaseRepository.findNewReleaseMbidWhereRelationshipsExist(offset, LIMIT).forEach(mbidConsumer);
			offset += LIMIT;
		}
	}

	public void autoQueryReleaseGroupsWithRelationships(@NotNull Consumer<UUID> mbidConsumer) {
		long count = releaseGroupRepository.countNewReleaseGroupsWhereRelationshipsExist();
		LOGGER.info("Found a total of {} new auto query release groups.", count);

		long offset = 0;
		while (offset < count) {
			LOGGER.info("Loading {} release groups with offset {}...", LIMIT, offset);
			releaseGroupRepository.findNewReleaseGroupsMbidWhereRelationshipsExist(offset, LIMIT).forEach(mbidConsumer);
			offset += LIMIT;
		}
	}

}
