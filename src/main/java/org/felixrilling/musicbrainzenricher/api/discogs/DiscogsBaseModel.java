package org.felixrilling.musicbrainzenricher.api.discogs;

/**
 * (Incomplete) Base class for discogs responses.
 */
abstract class DiscogsBaseModel {
    private long id;
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
