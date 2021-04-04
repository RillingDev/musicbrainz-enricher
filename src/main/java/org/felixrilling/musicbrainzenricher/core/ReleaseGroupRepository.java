package org.felixrilling.musicbrainzenricher.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class ReleaseGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    ReleaseGroupRepository(@Qualifier("musicbrainzLocalDbJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long countReleaseGroupsWhereRelationshipsExist() {
        return jdbcTemplate
                .queryForObject("SELECT COUNT(*) FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu)", Long.class);
    }

    public List<UUID> findReleaseGroupsMbidWhereRelationshipsExist(long offset, int limit) {
        return Collections.unmodifiableList(jdbcTemplate
                .query("SELECT rg.gid FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu) ORDER BY rg.id OFFSET ? LIMIT ?",
                        (rs, rowNum) -> rs.getObject("gid", UUID.class), offset, limit));
    }
}
