package org.felixrilling.musicbrainzenricher.core;

import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Not a 'real' repository because we only look up primitive values and not entities
@Component
public class ReleaseRepository {
	private final JdbcTemplate jdbcTemplate;

	ReleaseRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Long countReleasesWhereRelationshipsExist() {
		// TODO: check nullity
		return jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM musicbrainz.release r WHERE r.id IN (SELECT lru.entity0 FROM musicbrainz.l_release_url lru)",
			Long.class);
	}

	@NotNull
	public List<UUID> findReleaseMbidWhereRelationshipsExist(long offset, int limit) {
		List<UUID> mbids = jdbcTemplate.query(
			"SELECT r.gid FROM musicbrainz.release r WHERE r.id IN (SELECT lru.entity0 FROM musicbrainz.l_release_url lru) ORDER BY r.id OFFSET ? LIMIT ?",
			(rs, rowNum) -> rs.getObject("gid", UUID.class),
			offset,
			limit);
		return Collections.unmodifiableList(mbids);
	}
}
