package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import java.io.Serial;

public class MusicbrainzException extends Exception {
	@Serial
	private static final long serialVersionUID = 5573588744334378954L;

	MusicbrainzException(String message, Throwable cause) {
		super(message, cause);
	}
}
