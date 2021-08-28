package org.felixrilling.musicbrainzenricher.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class GenreRepository {

    private final JdbcTemplate jdbcTemplate;

    GenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<String> findGenreNames() {
        return Set.copyOf(jdbcTemplate.query("SELECT g.name FROM musicbrainz.genre g ORDER BY g.name",
                (rs, rowNum) -> rs.getString("name")));
    }

    public String findGenreNameByMbid(UUID mbid) {
        return jdbcTemplate
                .queryForObject("SELECT g.name FROM musicbrainz.genre g WHERE g.gid::TEXT LIKE ?", String.class, mbid.toString());
    }

}
