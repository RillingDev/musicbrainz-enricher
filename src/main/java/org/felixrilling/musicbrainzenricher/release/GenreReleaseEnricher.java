package org.felixrilling.musicbrainzenricher.release;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface GenreReleaseEnricher extends ReleaseEnricher {
    Set<String> fetchGenres(String relationUrl) throws Exception;
}
