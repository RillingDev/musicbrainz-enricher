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

	public long countNewReleaseGroupsWhereRelationshipsExist() {
		return Objects.requireNonNull(jdbcTemplate.queryForObject("""
			SELECT COUNT(*)	FROM musicbrainz.release_group rg
				WHERE rg.id IN
					(SELECT lrgu.entity0 FROM musicbrainz.l_release_group_url lrgu)
				AND rg.gid NOT IN
				  (SELECT he.mbid FROM musicbrainz_enricher.history_entry he WHERE he.data_type = 1)
			""", Long.class));
	}

	@NotNull
	public List<UUID> findNewReleaseGroupsMbidWhereRelationshipsExist(long offset, int limit) {
		List<UUID> mbids = jdbcTemplate.query("""
			SELECT rg.gid
			FROM musicbrainz.release_group rg
				WHERE rg.id IN
					  (SELECT lrgu.entity0 FROM musicbrainz.l_release_group_url	lrgu)
				AND rg.gid NOT IN
					  (SELECT he.mbid FROM musicbrainz_enricher.history_entry he WHERE he.data_type = 1)
			ORDER BY rg.id
			OFFSET ? LIMIT ?
			""", (rs, rowNum) -> rs.getObject("gid", UUID.class), offset, limit);
		return Collections.unmodifiableList(mbids);
	}
}
