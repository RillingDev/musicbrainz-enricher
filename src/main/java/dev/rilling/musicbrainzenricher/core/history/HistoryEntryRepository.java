package dev.rilling.musicbrainzenricher.core.history;

import dev.rilling.musicbrainzenricher.core.DataType;
import net.jcip.annotations.ThreadSafe;
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
				WHERE he.data_type = ?
				AND he.mbid = ?
				AND he.last_checked > ?
			""", Long.class, serializeDataType(dataType), mbid, serializeZonedDateTime(consideredActiveIfAfter)));

		return count > 0;
	}

	void persist(@NotNull HistoryEntry historyEntry) {
		jdbcTemplate.update("""
				INSERT INTO musicbrainz_enricher.history_entry (data_type, mbid, last_checked) VALUES (?, ?, ?)
				ON CONFLICT (data_type, mbid) DO UPDATE SET last_checked = excluded.last_checked
				""",
			serializeDataType(historyEntry.dataType()),
			historyEntry.mbid(),
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
