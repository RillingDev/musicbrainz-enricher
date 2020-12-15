package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.core.ReleaseGroupRepository;
import org.felixrilling.musicbrainzenricher.core.ReleaseRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.function.Consumer;

@Service
public class MusicbrainzAutoQueryService {

    private static final int LIMIT = 500;

    private final ReleaseRepository releaseRepository;
    private final ReleaseGroupRepository releaseGroupRepository;

    MusicbrainzAutoQueryService(ReleaseRepository releaseRepository, ReleaseGroupRepository releaseGroupRepository) {
        this.releaseRepository = releaseRepository;
        this.releaseGroupRepository = releaseGroupRepository;
    }

    public void autoQueryReleasesWithRelationships(@NotNull Consumer<String> mbidConsumer) {
        try {
            long count = releaseRepository.countReleasesWhereRelationshipsExist();
            long offset = 0;
            while (offset < count) {
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
            long offset = 0;
            while (offset < count) {
                releaseGroupRepository.findReleaseGroupsMbidWhereRelationshipsExist(offset, LIMIT).forEach(mbidConsumer);
                offset += LIMIT;
            }
        } catch (SQLException e) {
            throw new QueryException("Could not query release groups.", e);
        }
    }


}
