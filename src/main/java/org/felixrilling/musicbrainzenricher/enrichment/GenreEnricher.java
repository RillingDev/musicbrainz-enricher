package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Enricher with the capability of calculating genre data.
 *
 * @see org.felixrilling.musicbrainzenricher.enrichment.genre.GenreMatcherService
 */
@Component
public interface GenreEnricher extends Enricher {

    /**
     * Returns a set of musicbrainz compatible genre names that belong to the relation target.
     *
     * @param relationUrl Relation URL.
     * @return a set of genres.
     */
    @NotNull Set<String> fetchGenres(@NotNull String relationUrl);
}
