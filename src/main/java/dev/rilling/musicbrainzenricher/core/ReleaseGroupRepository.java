package dev.rilling.musicbrainzenricher.core;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@ThreadSafe
public class ReleaseGroupRepository implements WorkQueueRepository {

	private final JdbcClient jdbcClient;

	public ReleaseGroupRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public long countWorkQueue() {
		return jdbcClient.sql("SELECT COUNT(*) FROM musicbrainz_enricher.release_group_work_queue").query(Long.class).single();
	}

	@Override
	public  List<UUID> queryWorkQueue(int limit) {
		return jdbcClient.sql("SELECT gid FROM musicbrainz_enricher.release_group_work_queue LIMIT ?").param(limit).query(UUID.class).list();
	}

	@Override
	public  DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}
}
