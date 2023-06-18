package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.core.ReleaseGroupRepository;
import dev.rilling.musicbrainzenricher.core.ReleaseRepository;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@ThreadSafe
public class MusicbrainzAutoQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzAutoQueryService.class);

	private static final int LIMIT = 100;

	private final ReleaseRepository releaseRepository;
	private final ReleaseGroupRepository releaseGroupRepository;


	MusicbrainzAutoQueryService(ReleaseRepository releaseRepository, ReleaseGroupRepository releaseGroupRepository) {
		this.releaseRepository = releaseRepository;
		this.releaseGroupRepository = releaseGroupRepository;
	}

	public void autoQueryReleases(@NotNull Consumer<UUID> mbidConsumer) {
		long count = releaseRepository.countFromWorkQueue();
		while (count > 0) {
			LOGGER.info("{} auto query releases remaining.", count);
			releaseRepository.findFromWorkQueue(LIMIT).forEach(mbidConsumer);
			count = releaseRepository.countFromWorkQueue();
		}
	}

	public void autoQueryReleaseGroups(@NotNull Consumer<UUID> mbidConsumer) {
		long count = releaseGroupRepository.countFromWorkQueue();
		while (count > 0) {
			LOGGER.info("{} auto query release groups remaining.", count);
			releaseGroupRepository.findFromWorkQueue(LIMIT).forEach(mbidConsumer);
			count = releaseGroupRepository.countFromWorkQueue();
		}
	}

}
