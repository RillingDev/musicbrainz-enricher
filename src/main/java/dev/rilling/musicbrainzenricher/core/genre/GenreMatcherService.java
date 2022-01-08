package dev.rilling.musicbrainzenricher.core.genre;

import dev.rilling.musicbrainzenricher.util.CanonicalStringMatcher;
import dev.rilling.musicbrainzenricher.util.StringVariantChecker;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@ThreadSafe
public class GenreMatcherService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenreMatcherService.class);

	private static final StringVariantChecker STRING_VARIANT_CHECKER;

	static {
		Set<String> delimiters = Set.of("-", " ", " and ", " & ");
		Collator collator = Collator.getInstance(Locale.ROOT);
		collator.setStrength(Collator.PRIMARY);
		STRING_VARIANT_CHECKER = new StringVariantChecker(delimiters, collator);
	}

	private final AtomicReference<CanonicalStringMatcher> canonicalStringMatcherRef = new AtomicReference<>(null);

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
		/*
		 * It is possible for two threads to concurrently try to initialize the string matcher.
		 * If that happens, the first one wins, with any further initialization still starting but never being applied.
		 */
		if (canonicalStringMatcherRef.get() == null) {
			Set<String> canonicalGenres = genreRepository.findGenreNames();
			CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalGenres,
				STRING_VARIANT_CHECKER);
			canonicalStringMatcherRef.compareAndSet(null, canonicalStringMatcher);
		}
		return canonicalStringMatcherRef.get();
	}
}
