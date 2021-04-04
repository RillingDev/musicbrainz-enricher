package org.felixrilling.musicbrainzenricher.core.genre;

import org.felixrilling.musicbrainzenricher.core.GenreRepository;
import org.felixrilling.musicbrainzenricher.util.StringVariantChecker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenreMatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenreMatcherService.class);

    private static final StringVariantChecker STRING_VARIANT_CHECKER = new StringVariantChecker(Set.of("-", " ", " and ", " & "));

    private final GenreRepository genreRepository;

    GenreMatcherService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    /**
     * Finds the associated canonical genre names for the provided genres.
     * If no good match is found, it is dropped from the final result.
     *
     * @param unmatchedGenres Unmatched genres to look up canonical genre names for.
     * @return Matching canonical genre names.
     */
    public @NotNull Set<String> match(@NotNull Set<String> unmatchedGenres) {
        Set<String> knownGenres = genreRepository.findGenreNames();

        Set<String> matches = new HashSet<>(unmatchedGenres.size());
        for (String unmatchedGenre : unmatchedGenres) {
            matchSingle(knownGenres, unmatchedGenre).ifPresent(matches::add);
        }
        LOGGER.trace("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

        return Collections.unmodifiableSet(matches);
    }

    /**
     * Finds the fitting known genre name for any given genre name.
     *
     * @param knownGenres    Known genre names.
     * @param unmatchedGenre Unmatched genre to look up canonical genre name for.
     * @return Matching canonical genre name. Empty if no match exists (unknown genre).
     */
    private Optional<String> matchSingle(@NotNull Set<String> knownGenres, @NotNull String unmatchedGenre) {
        return knownGenres.stream().filter(knownGenre -> STRING_VARIANT_CHECKER.isVariant(knownGenre, unmatchedGenre)).findFirst();
    }
}
