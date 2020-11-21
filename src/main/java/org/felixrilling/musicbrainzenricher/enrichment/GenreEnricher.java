package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public interface GenreEnricher extends Enricher {
    @NotNull Set<String> fetchGenres(@NotNull String relationUrl);
}
