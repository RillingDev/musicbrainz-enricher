package org.felixrilling.musicbrainzenricher.core.history;

import net.jcip.annotations.ThreadSafe;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
@ThreadSafe
class HistoryEntryRepository {

	private final JdbcTemplate jdbcTemplate;

	HistoryEntryRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	boolean hasActiveEntry(@NotNull DataType dataType,
						   @NotNull UUID mbid,
						   @NotNull ZonedDateTime consideredActiveIfAfter) {
		Long count = Objects.requireNonNull(jdbcTemplate.queryForObject("""
			SELECT COUNT(*) FROM musicbrainz_enricher.history_entry he
				WHERE he.mbid = ?
				AND he.data_type = ?
				AND he.last_checked > ?
			""", Long.class, mbid, serializeDataType(dataType), serializeZonedDateTime(consideredActiveIfAfter)));

		return count > 0;
	}

	void persist(@NotNull HistoryEntry historyEntry) {
		jdbcTemplate.update("""
				INSERT INTO musicbrainz_enricher.history_entry (mbid, data_type, last_checked) VALUES (?, ?, ?)
				ON CONFLICT (mbid, data_type) DO UPDATE SET last_checked = excluded.last_checked
				""",
			historyEntry.mbid(),
			serializeDataType(historyEntry.dataType()),
			serializeZonedDateTime(historyEntry.lastChecked()));
	}

	@NotNull
	private OffsetDateTime serializeZonedDateTime(@NotNull ZonedDateTime before) {
		return before.toOffsetDateTime();
	}

	private int serializeDataType(@NotNull DataType dataType) {
		return dataType.ordinal();
	}
}
