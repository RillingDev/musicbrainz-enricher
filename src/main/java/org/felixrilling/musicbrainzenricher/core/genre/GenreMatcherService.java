package org.felixrilling.musicbrainzenricher.core.genre;

import org.felixrilling.musicbrainzenricher.core.GenreRepository;
import org.felixrilling.musicbrainzenricher.util.StringVariantChecker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GenreMatcherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenreMatcherService.class);

	private static final Collator COLLATOR;
	private static final StringVariantChecker STRING_VARIANT_CHECKER;

	static {
		COLLATOR = Collator.getInstance(Locale.ROOT);
		COLLATOR.setStrength(Collator.PRIMARY);
		STRING_VARIANT_CHECKER = new StringVariantChecker(Set.of("-", " ", " and ", " & "), COLLATOR);
	}

	private final GenreRepository genreRepository;

	// Primitive cache for unmatched genre -> match mapping.
	// Currently no invalidation available as known genres should not change during runtime.
	private final Map<String, Optional<String>> matchedGenres = new ConcurrentHashMap<>(500);

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
		Set<String> matches = new HashSet<>(unmatchedGenres.size());
		for (String unmatchedGenre : unmatchedGenres) {
			matchedGenres.computeIfAbsent(unmatchedGenre, this::matchSingle).ifPresent(matches::add);
		}
		LOGGER.debug("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

		return Collections.unmodifiableSet(matches);
	}

	/**
	 * Finds the fitting known genre name for any given genre name.
	 *
	 * @param unmatchedGenre Unmatched genre to look up canonical genre name for.
	 * @return Matching canonical genre name. Empty if no match exists (unknown genre).
	 */
	private Optional<String> matchSingle(@NotNull String unmatchedGenre) {
		Optional<String> match = genreRepository.findGenreNames()
			.stream()
			.filter(knownGenre -> STRING_VARIANT_CHECKER.isVariant(knownGenre, unmatchedGenre))
			.findFirst();
		LOGGER.trace("Matched genre '{}' to '{}'.", unmatchedGenre, match);
		return match;
	}

}
