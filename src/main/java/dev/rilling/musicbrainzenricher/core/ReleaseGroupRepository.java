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
public class ReleaseGroupRepository {

	private final JdbcTemplate jdbcTemplate;

	ReleaseGroupRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long countFromWorkQueue() {
		return Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM musicbrainz_enricher.release_group_work_queue", Long.class));
	}

	@NotNull
	public List<UUID> findFromWorkQueue(long offset) {
		List<UUID> mbids = jdbcTemplate.query("SELECT rg.gid FROM musicbrainz_enricher.release_group_work_queue rg LIMIT ?", (rs, rowNum) -> rs.getObject("gid", UUID.class), offset);
		return Collections.unmodifiableList(mbids);
	}
}
