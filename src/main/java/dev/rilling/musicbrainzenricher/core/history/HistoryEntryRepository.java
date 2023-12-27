package dev.rilling.musicbrainzenricher.core.history;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ThreadSafe
class HistoryEntryRepository {

	private final JdbcClient jdbcClient;

	HistoryEntryRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	void persist(HistoryEntry historyEntry) {
		switch (historyEntry.dataType()) {
			case RELEASE -> jdbcClient.sql("""
				INSERT INTO musicbrainz_enricher.release_history_entry (release_gid) VALUES (?)
				ON CONFLICT (release_gid) DO NOTHING
				""").param(historyEntry.mbid()).update();
			case RELEASE_GROUP -> jdbcClient.sql("""
				INSERT INTO musicbrainz_enricher.release_group_history_entry (release_group_gid) VALUES (?)
				ON CONFLICT (release_group_gid) DO NOTHING
				""").param(historyEntry.mbid()).update();
		}
	}
}
