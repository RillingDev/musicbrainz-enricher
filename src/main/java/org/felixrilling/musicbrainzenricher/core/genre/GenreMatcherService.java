package org.felixrilling.musicbrainzenricher.core.genre;

import org.felixrilling.musicbrainzenricher.core.GenreRepository;
import org.felixrilling.musicbrainzenricher.util.CanonicalStringMatcher;
import org.felixrilling.musicbrainzenricher.util.StringVariantChecker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenreMatcherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenreMatcherService.class);

	private static final StringVariantChecker STRING_VARIANT_CHECKER;

	static {
		Collator collator = Collator.getInstance(Locale.ROOT);
		collator.setStrength(Collator.PRIMARY);
		STRING_VARIANT_CHECKER = new StringVariantChecker(Set.of("-", " ", " and ", " & "), collator);
	}

	private final GenreRepository genreRepository;
	private CanonicalStringMatcher canonicalStringMatcher;

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
	@NotNull
	public Set<String> match(@NotNull Set<String> unmatchedGenres) {
		Set<String> matches = unmatchedGenres.stream()
			.map(getCanonicalStringMatcher()::canonicalize)
			.flatMap(Optional::stream)
			.collect(Collectors.toUnmodifiableSet());

		LOGGER.debug("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

		return matches;
	}

	@NotNull
	private CanonicalStringMatcher getCanonicalStringMatcher() {
		// Lazy-load to load genres after initial construction.
		// Not atomic, but running this twice does not really matter.
		if (canonicalStringMatcher == null) {
			canonicalStringMatcher = new CanonicalStringMatcher(genreRepository.findGenreNames(),
				STRING_VARIANT_CHECKER);
		}
		return canonicalStringMatcher;
	}
}
