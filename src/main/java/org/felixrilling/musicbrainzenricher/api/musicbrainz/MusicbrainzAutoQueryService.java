package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.core.ReleaseGroupRepository;
import org.felixrilling.musicbrainzenricher.core.ReleaseRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.function.Consumer;

@Service
public class MusicbrainzAutoQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzAutoQueryService.class);

    private static final int LIMIT = 100;

    private final ReleaseRepository releaseRepository;
    private final ReleaseGroupRepository releaseGroupRepository;

    MusicbrainzAutoQueryService(ReleaseRepository releaseRepository, ReleaseGroupRepository releaseGroupRepository) {
        this.releaseRepository = releaseRepository;
        this.releaseGroupRepository = releaseGroupRepository;
    }

    public void autoQueryReleasesWithRelationships(@NotNull Consumer<String> mbidConsumer) {
        try {
            long count = releaseRepository.countReleasesWhereRelationshipsExist();
            logger.info("Found a total of {} releases with at least one relationship.", count);

            long offset = 0;
            while (offset < count) {
                logger.debug("Loading {} releases with offset {} with at least one relationship...", LIMIT, offset);
                releaseRepository.findReleaseMbidWhereRelationshipsExist(offset, LIMIT).forEach(mbidConsumer);
                offset += LIMIT;
            }
        } catch (SQLException e) {
            throw new QueryException("Could not query releases.", e);
        }
    }

    public void autoQueryReleaseGroupsWithRelationships(@NotNull Consumer<String> mbidConsumer) {
        try {
            long count = releaseGroupRepository.countReleaseGroupsWhereRelationshipsExist();
            logger.info("Found a total of {} release groups with at least one relationship.", count);

            long offset = 0;
            while (offset < count) {
                logger.debug("Loading {} release groups with offset {} with at least one relationship...", LIMIT, offset);
                releaseGroupRepository.findReleaseGroupsMbidWhereRelationshipsExist(offset, LIMIT).forEach(mbidConsumer);
                offset += LIMIT;
            }
        } catch (SQLException e) {
            throw new QueryException("Could not query release groups.", e);
        }
    }


}
