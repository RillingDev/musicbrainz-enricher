package dev.rilling.musicbrainzenricher.core.history;

import net.jcip.annotations.ThreadSafe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ThreadSafe
class HistoryEntryRepository {

	private final JdbcTemplate jdbcTemplate;

	HistoryEntryRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	void persist( HistoryEntry historyEntry) {
		switch (historyEntry.dataType()) {
			case RELEASE -> jdbcTemplate.update("""
				INSERT INTO musicbrainz_enricher.release_history_entry (release_gid) VALUES (?)
				ON CONFLICT (release_gid) DO NOTHING
				""", historyEntry.mbid());
			case RELEASE_GROUP -> jdbcTemplate.update("""
				INSERT INTO musicbrainz_enricher.release_group_history_entry (release_group_gid) VALUES (?)
				ON CONFLICT (release_group_gid) DO NOTHING
				""", historyEntry.mbid());
			default -> throw new IllegalArgumentException("Invalid entry: '%s'.".formatted(historyEntry));
		}
	}
}
