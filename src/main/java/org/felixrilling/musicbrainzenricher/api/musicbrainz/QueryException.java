package org.felixrilling.musicbrainzenricher.api.musicbrainz;

class QueryException extends RuntimeException {
    QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
