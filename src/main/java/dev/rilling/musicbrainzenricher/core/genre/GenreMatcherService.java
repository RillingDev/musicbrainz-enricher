package dev.rilling.musicbrainzenricher.core.genre;

import dev.rilling.musicbrainzenricher.util.CanonicalStringMatcher;
import dev.rilling.musicbrainzenricher.util.StringVariantChecker;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ThreadSafe
public class GenreMatcherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenreMatcherService.class);

	private final CanonicalStringMatcher canonicalStringMatcher;


	GenreMatcherService(GenreRepository genreRepository) {
		canonicalStringMatcher = createCanonicalStringMatcher(genreRepository.findGenreNames());
	}

	/**
	 * Finds the associated canonical genre names for the provided genres.
	 * If no good match is found, it is dropped from the result.
	 *
	 * @param unmatchedGenres Unmatched genres to look up canonical genre names for.
	 * @return Matching canonical genre names.
	 */

	public Set<String> match(Set<String> unmatchedGenres) {
		if (unmatchedGenres.isEmpty()) {
			return Set.of();
		}

		Set<String> matches = unmatchedGenres.stream()
			.map(canonicalStringMatcher::canonicalize)
			.flatMap(Optional::stream)
			.collect(Collectors.toUnmodifiableSet());

		LOGGER.debug("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

		return matches;
	}


	private static CanonicalStringMatcher createCanonicalStringMatcher(Set<String> canonicalGenreNames) {
		Set<String> delimiters = Set.of("-", " ", " and ", " & ");
		Collator collator = Collator.getInstance(Locale.ROOT);
		collator.setStrength(Collator.PRIMARY);
		StringVariantChecker stringVariantChecker = new StringVariantChecker(delimiters, collator);

		return new CanonicalStringMatcher(canonicalGenreNames, stringVariantChecker);
	}
}
