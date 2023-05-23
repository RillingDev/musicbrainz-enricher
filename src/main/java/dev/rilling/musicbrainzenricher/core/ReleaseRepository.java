package dev.rilling.musicbrainzenricher.core;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@ThreadSafe
public class ReleaseRepository {
	private final JdbcTemplate jdbcTemplate;

	ReleaseRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long countFromWorkQueue() {
		return Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM musicbrainz_enricher.release_work_queue", Long.class));
	}

	@NotNull
	public List<UUID> findFromWorkQueue(int limit) {
		List<UUID> mbids = jdbcTemplate.query("SELECT r.gid FROM musicbrainz_enricher.release_work_queue r LIMIT ?", (rs, rowNum) -> rs.getObject("gid", UUID.class), limit);
		return Collections.unmodifiableList(mbids);
	}
}
