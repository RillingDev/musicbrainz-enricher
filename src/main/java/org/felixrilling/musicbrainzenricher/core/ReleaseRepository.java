package org.felixrilling.musicbrainzenricher.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class ReleaseRepository {
    private final JdbcTemplate jdbcTemplate;

    ReleaseRepository(@Qualifier("musicbrainzLocalDbJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long countReleasesWhereRelationshipsExist() throws SQLException {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM release r WHERE r.id IN (SELECT lru.entity0 FROM l_release_url lru)", Long.class);
    }

    public List<UUID> findReleaseMbidWhereRelationshipsExist(long offset, int limit) throws SQLException {
        return Collections.unmodifiableList(jdbcTemplate
                .query("SELECT r.gid FROM release r WHERE r.id IN (SELECT lru.entity0 FROM l_release_url lru) ORDER BY r.id OFFSET ? LIMIT ?",
                        (rs, rowNum) -> rs.getObject("gid", UUID.class), offset, limit));
    }
}
