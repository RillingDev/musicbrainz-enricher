package dev.rilling.musicbrainzenricher.enrichment;


import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ThreadSafe
public class EntityEnrichmentResultRepository {
	private final JdbcClient jdbcClient;
	private final JdbcTemplate jdbcTemplate;

	EntityEnrichmentResultRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
		this.jdbcClient = jdbcClient;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void persistReleaseGroupResult(EntityEnrichmentResult entityEnrichmentResult) {
		List<Object[]> batch = new ArrayList<>();
		for (EntityEnrichmentResult.RelationEnrichmentResult result : entityEnrichmentResult.results()) {
			for (String genre : result.genres()) {
				UUID releaseGroupGid = UUID.fromString(entityEnrichmentResult.targetEntity().getId());
				batch.add(new Object[]{releaseGroupGid, genre});
			}
		}
		jdbcTemplate.batchUpdate("INSERT INTO musicbrainz_enricher.enricher_release_group_result (release_group_gid, genre_name) VALUES(?,?)", batch);
	}
}
