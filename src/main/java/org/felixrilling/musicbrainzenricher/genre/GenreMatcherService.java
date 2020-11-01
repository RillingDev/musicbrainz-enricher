package org.felixrilling.musicbrainzenricher.genre;

import org.apache.commons.text.similarity.LevenshteinDistance;
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

    private static final double SHORT_STRING_PENALTY_BASE = 5.0;
    private static final double WEIGHTED_DISTANCE_LIMIT = 2.0;

    private final LevenshteinDistance levenshteinDistance = LevenshteinDistance
            .getDefaultInstance();
    private final GenreProviderService genreProviderService;

    GenreMatcherService(GenreProviderService genreProviderService) {
        this.genreProviderService = genreProviderService;
    }

    public Set<String> match(Set<String> unmatchedGenres) {
        Set<String> matches = new HashSet<>();

        for (String unmatchedGenre : unmatchedGenres) {
            matchSingle(unmatchedGenre).ifPresent(matches::add);
        }
        logger.debug("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

        return Collections.unmodifiableSet(matches);
    }

    private Optional<String> matchSingle(String unmatchedGenre) {
        for (String knownGenre : genreProviderService.getGenres()) {
            int distance = levenshteinDistance.apply(knownGenre, unmatchedGenre);
            // Penalise short strings, due the impact a single difference has (e.g. 'idm' and 'edm').
            double lengthPenalty = SHORT_STRING_PENALTY_BASE / unmatchedGenre.length();
            if (distance + lengthPenalty <= WEIGHTED_DISTANCE_LIMIT) {
                return Optional.of(knownGenre);
            }
        }
        return Optional.empty();
    }
}
