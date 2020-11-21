package org.felixrilling.musicbrainzenricher.enrichment;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.QueryException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public interface EnrichmentService {
    void enrichRelease(@NotNull String mbid) throws QueryException;
}
