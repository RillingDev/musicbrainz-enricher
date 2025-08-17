package dev.rilling.musicbrainzenricher.core.history;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ThreadSafe
public class HistoryEntryRepository {

	private final JdbcClient jdbcClient;

	HistoryEntryRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public void persist(HistoryEntry historyEntry) {
		switch (historyEntry.dataType()) {
			case RELEASE ->
				jdbcClient.sql("INSERT INTO musicbrainz_enricher.release_history_entry (release_gid) VALUES (?)").param(historyEntry.sourceMbid()).update();
			case RELEASE_GROUP ->
				jdbcClient.sql("INSERT INTO musicbrainz_enricher.release_group_history_entry (release_group_gid) VALUES (?)").param(historyEntry.sourceMbid()).update();
		}
	}
}
