package dev.rilling.musicbrainzenricher.core.genre;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@ThreadSafe
public class GenreRepository {

	private final JdbcTemplate jdbcTemplate;

	GenreRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public Set<String> findGenreNames() {
		List<String> genreNames = jdbcTemplate.query("""
			SELECT g.name FROM musicbrainz.genre g
			ORDER BY g.name
			""", (rs, rowNum) -> rs.getString("name"));
		return Set.copyOf(genreNames);
	}

	public Optional<String> findGenreNameByMbid( UUID mbid) {
		return Optional.ofNullable(jdbcTemplate.queryForObject("""
			SELECT g.name FROM musicbrainz.genre g
				WHERE g.gid = ?
			""", String.class, mbid));
	}

}
