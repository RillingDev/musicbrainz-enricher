package org.felixrilling.musicbrainzenricher.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class GenreRepository {

    private final JdbcTemplate jdbcTemplate;

    GenreRepository(@Qualifier("musicbrainzLocalDbJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> findGenreNames() {
        return Collections.unmodifiableList(jdbcTemplate.query("SELECT g.name FROM genre g ORDER BY g.name",
                (rs, rowNum) -> rs.getString("name")));
    }

    public String findGenreNameByMbid(String mbid) throws SQLException {
        return jdbcTemplate
                .queryForObject("SELECT g.name FROM genre g WHERE g.gid::TEXT LIKE ?", String.class, mbid);
    }

}
