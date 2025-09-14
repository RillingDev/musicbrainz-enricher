package dev.rilling.musicbrainzenricher.enrichment;


import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@ThreadSafe
public class ReleaseGroupEnrichmentResultRepository {
	private final JdbcClient jdbcClient;
	private final JdbcTemplate jdbcTemplate;

	ReleaseGroupEnrichmentResultRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
		this.jdbcClient = jdbcClient;
		this.jdbcTemplate = jdbcTemplate;
	}


	@Transactional
	public void persistResults(Collection<ReleaseGroupEnrichmentResult> result) {
		List<Object[]> batch = result.stream().map(r -> new Object[]{r.targetMbid(), r.sourceUrl(), r.genre()}).toList();
		jdbcTemplate.batchUpdate("INSERT INTO musicbrainz_enricher.enricher_release_group_result (target_release_group_gid, source_url, genre_name) VALUES(?, ?, ?)", batch);
	}


	public List<ReleaseGroupEnrichmentResult> findMergedResults(int limit, int offset) {
		return jdbcClient.sql("SELECT target_release_group_gid, genre_name FROM musicbrainz_enricher.enricher_release_group_result_merged LIMIT ? OFFSET ?")
			.param(1, limit)
			.param(2, offset)
			.query((rs, rowNum) -> new ReleaseGroupEnrichmentResult(rs.getObject(1, UUID.class), "<OMITTED>", rs.getString(2)))
			.list();
	}

}
