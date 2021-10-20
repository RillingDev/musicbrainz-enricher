package org.felixrilling.musicbrainzenricher.core.history;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "history_entry", schema = "musicbrainz_enricher")
class HistoryEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private long id;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "data_type", nullable = false, updatable = false)
	@NotNull
	private DataType dataType;

	@Column(name = "mbid", nullable = false, updatable = false)
	@NotNull
	private UUID mbid;

	@Column(name = "last_checked", nullable = false)
	@NotNull
	private ZonedDateTime lastChecked;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public @NotNull DataType getDataType() {
		return dataType;
	}

	public void setDataType(@NotNull DataType dataType) {
		this.dataType = dataType;
	}

	public @NotNull UUID getMbid() {
		return mbid;
	}

	public void setMbid(@NotNull UUID mbid) {
		this.mbid = mbid;
	}

	public @NotNull ZonedDateTime getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(@NotNull ZonedDateTime lastChecked) {
		this.lastChecked = lastChecked;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		HistoryEntry that = (HistoryEntry) obj;
		return id == that.id && dataType == that.dataType && mbid.equals(that.mbid) &&
			lastChecked.equals(that.lastChecked);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, dataType, mbid, lastChecked);
	}

	@Override
	public String toString() {
		return "HistoryEntry{" + "id=" + id + ", dataType=" + dataType + ", mbid='" + mbid + '\'' + ", lastChecked=" +
			lastChecked + '}';
	}
}
