package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
class ReleaseGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    ReleaseGroupRepository(@Qualifier("musicbrainzLocalDbJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long countReleaseGroupsWhereRelationshipsExist() throws SQLException {
        return jdbcTemplate
                .queryForObject("SELECT COUNT(*) FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu)", Long.class);
    }

    public List<String> findReleaseGroupsMbidWhereRelationshipsExist(long offset, int limit) throws SQLException {
        return Collections.unmodifiableList(jdbcTemplate
                .query("SELECT rg.gid FROM release_group rg WHERE rg.id IN (SELECT lrgu.entity0 FROM l_release_group_url lrgu) OFFSET ? LIMIT ?",
                        (rs, rowNum) -> rs.getString("gid"), offset, limit));
    }
}
