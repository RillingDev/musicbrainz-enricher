package org.felixrilling.musicbrainzenricher.history;

import org.felixrilling.musicbrainzenricher.DataType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "history_entry",
        uniqueConstraints = @UniqueConstraint(
                name = "history_entry_unique",
                columnNames = {"data_type", "mbid"}
        )
)
class HistoryEntry {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;

    @Column(name = "mbid", nullable = false)
    private String mbid;

    @Column(name = "last_checked", nullable = false)
    private ZonedDateTime lastChecked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public ZonedDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(ZonedDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoryEntry that = (HistoryEntry) o;
        return id == that.id &&
                dataType == that.dataType &&
                mbid.equals(that.mbid) &&
                lastChecked.equals(that.lastChecked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataType, mbid, lastChecked);
    }

    @Override
    public String toString() {
        return "HistoryEntry{" +
                "id=" + id +
                ", dataType=" + dataType +
                ", mbid='" + mbid + '\'' +
                ", lastChecked=" + lastChecked +
                '}';
    }
}
