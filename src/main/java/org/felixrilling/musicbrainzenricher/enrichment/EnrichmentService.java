package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public interface EnrichmentService {
    void enrich(@NotNull String mbid);
}
