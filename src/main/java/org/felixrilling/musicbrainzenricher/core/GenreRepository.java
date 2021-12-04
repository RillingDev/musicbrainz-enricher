package org.felixrilling.musicbrainzenricher.core;

import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class GenreRepository {

	private final JdbcTemplate jdbcTemplate;

	GenreRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@NotNull
	public Set<String> findGenreNames() {
		List<String> genreNames = jdbcTemplate.query("SELECT g.name FROM musicbrainz.genre g ORDER BY g.name",
			(rs, rowNum) -> rs.getString("name"));
		return Set.copyOf(genreNames);
	}

	public String findGenreNameByMbid(@NotNull UUID mbid) {
		// TODO: check nullity
		return jdbcTemplate.queryForObject("SELECT g.name FROM musicbrainz.genre g WHERE g.gid::TEXT LIKE ?",
			String.class,
			mbid.toString());
	}

}
