package org.felixrilling.musicbrainzenricher.api.musicbrainz;

public class QueryException extends Exception {
    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
