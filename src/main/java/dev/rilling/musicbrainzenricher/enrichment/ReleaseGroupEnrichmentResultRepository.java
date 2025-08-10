package dev.rilling.musicbrainzenricher.enrichment;


import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
		List<Object[]> batch = result.stream().map(r -> new Object[]{r.gid(), r.genre()}).toList();
		// TODO merge duplicates and/or link to history entry to make resuming a stopped process easier
		jdbcTemplate.batchUpdate("INSERT INTO musicbrainz_enricher.enricher_release_group_result (release_group_gid, genre_name) VALUES(?, ?)", batch);
	}


	public Stream<ReleaseGroupEnrichmentResult> findMergedResults() {
		return jdbcClient.sql("SELECT release_group_gid, genre_name FROM musicbrainz_enricher.enricher_release_group_result_merged").query((rs, rowNum) -> new ReleaseGroupEnrichmentResult(rs.getObject(1, UUID.class), rs.getString(2))).stream();
	}

}
