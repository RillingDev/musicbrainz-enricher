package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Repository
@ThreadSafe
class HistoryEntryRepository {

	private final JdbcTemplate jdbcTemplate;

	HistoryEntryRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	void persist(@NotNull HistoryEntry historyEntry) {
		jdbcTemplate.update("""
			INSERT INTO musicbrainz_enricher.history_entry (data_type, mbid) VALUES (?, ?)
			ON CONFLICT (data_type, mbid) DO NOTHING
			""", serializeDataType(historyEntry.dataType()), historyEntry.mbid());
	}

	@NotNull
	private OffsetDateTime serializeZonedDateTime(@NotNull ZonedDateTime before) {
		return before.toOffsetDateTime();
	}

	private int serializeDataType(@NotNull DataType dataType) {
		return dataType.ordinal();
	}
}
