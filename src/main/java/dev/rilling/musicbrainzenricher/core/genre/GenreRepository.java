package dev.rilling.musicbrainzenricher.core.genre;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@ThreadSafe
public class GenreRepository {

	private final JdbcClient jdbcClient;

	public GenreRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}


	public Set<String> findGenreNames() {
		return jdbcClient.sql("SELECT name FROM musicbrainz.genre").query(String.class).set();
	}

	public Optional<String> findGenreNameByMbid(UUID mbid) {
		return jdbcClient.sql("SELECT name FROM musicbrainz.genre WHERE gid = ?").param(mbid).query(String.class).optional();
	}

}
