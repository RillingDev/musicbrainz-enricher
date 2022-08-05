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
// TODO: Rewrite queries to perform better
public class ReleaseRepository {
	private final JdbcTemplate jdbcTemplate;

	ReleaseRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long countNewReleasesWhereRelationshipsExist() {
		return Objects.requireNonNull(jdbcTemplate.queryForObject("""
			SELECT COUNT(*) FROM musicbrainz.release r
				WHERE r.id IN
					(SELECT lru.entity0 FROM musicbrainz.l_release_url lru)
				AND r.gid NOT IN
				  (SELECT he.mbid FROM musicbrainz_enricher.history_entry he WHERE he.data_type = 0)
			""", Long.class));
	}

	@NotNull
	public List<UUID> findNewReleaseMbidWhereRelationshipsExist(long offset, int limit) {
		List<UUID> mbids = jdbcTemplate.query("""
			SELECT r.gid FROM musicbrainz.release r
				WHERE r.id IN
					(SELECT lru.entity0 FROM musicbrainz.l_release_url lru)
				AND r.gid NOT IN
				  (SELECT he.mbid FROM musicbrainz_enricher.history_entry he WHERE he.data_type = 0)
			ORDER BY r.id
			OFFSET ? LIMIT ?
			""", (rs, rowNum) -> rs.getObject("gid", UUID.class), offset, limit);
		return Collections.unmodifiableList(mbids);
	}
}
