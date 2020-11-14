package org.felixrilling.musicbrainzenricher.genre;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class GenreMatcherService {

    private static final Logger logger = LoggerFactory.getLogger(GenreMatcherService.class);

    private static final double SIMILARITY_MINIMUM = 0.85;

    private final NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    private final GenreProviderService genreProviderService;

    GenreMatcherService(GenreProviderService genreProviderService) {
        this.genreProviderService = genreProviderService;
    }

    /**
     * Finds the associated canonical genre names for the provided genres.
     * If no good match is found it is dropped from the final result.
     *
     * @param unmatchedGenres Unmatched genres to look up canonical genre names for.
     * @return Matching canonical genre names.
     */
    public @NotNull Set<String> match(@NotNull Set<String> unmatchedGenres) {
        Set<String> matches = new HashSet<>();

        for (String unmatchedGenre : unmatchedGenres) {
            matchSingle(unmatchedGenre).ifPresent(matches::add);
        }
        logger.trace("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

        return Collections.unmodifiableSet(matches);
    }

    /**
     * Finds the first of genres the highest normalized levenshtein similarity,
     * but at least {@link #SIMILARITY_MINIMUM}.
     *
     * @param unmatchedGenre Unmatched genre to look up canonical genre name for.
     * @return Matching canonical genre name.
     */
    private Optional<String> matchSingle(@NotNull String unmatchedGenre) {
        String bestMatch = null;
        double bestMatchSimilarity = 0.0;
        for (String knownGenre : genreProviderService.getGenres()) {
            double similarity = normalizedLevenshtein.similarity(knownGenre.toLowerCase(), unmatchedGenre.toLowerCase());
            if (similarity > bestMatchSimilarity && similarity >= SIMILARITY_MINIMUM) {
                bestMatch = knownGenre;
                bestMatchSimilarity = similarity;
            }
        }
        return Optional.ofNullable(bestMatch);
    }
}
