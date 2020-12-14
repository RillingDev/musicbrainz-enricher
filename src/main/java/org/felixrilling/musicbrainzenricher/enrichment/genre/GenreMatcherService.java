package org.felixrilling.musicbrainzenricher.enrichment.genre;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.GenreRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenreMatcherService {

    private static final Logger logger = LoggerFactory.getLogger(GenreMatcherService.class);

    private static final double SIMILARITY_MINIMUM = 0.85;
    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();

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
        List<String> knownGenres = genreRepository.findGenreNames();

        Set<String> matches = new HashSet<>();
        for (String unmatchedGenre : unmatchedGenres) {
            matchSingle(knownGenres, unmatchedGenre).ifPresent(matches::add);
        }
        logger.trace("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

        return Collections.unmodifiableSet(matches);
    }

    /**
     * Finds the first of genres the highest normalized levenshtein similarity,
     * but at least {@link #SIMILARITY_MINIMUM}.
     *
     * @param knownGenres    Known genre names.
     * @param unmatchedGenre Unmatched genre to look up canonical genre name for.
     * @return Matching canonical genre name.
     */
    private Optional<String> matchSingle(@NotNull List<String> knownGenres, @NotNull String unmatchedGenre) {
        String bestMatch = null;
        double bestMatchSimilarity = 0.0;
        for (String knownGenre : knownGenres) {
            double similarity = normalizedSimilarity(knownGenre.toLowerCase(), unmatchedGenre.toLowerCase());
            if (similarity > bestMatchSimilarity && similarity >= SIMILARITY_MINIMUM) {
                bestMatch = knownGenre;
                bestMatchSimilarity = similarity;
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    // https://stackoverflow.com/a/16018452/6454249
    private double normalizedSimilarity(String s1, String s2) {
        String longer = s1;
        String shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        return (longerLength - LEVENSHTEIN.apply(longer, shorter)) / (double) longerLength;
    }
}
