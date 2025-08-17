package dev.rilling.musicbrainzenricher.core.genre;

import net.jcip.annotations.ThreadSafe;
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


	private final AtomicReference<CanonicalStringMatcher> canonicalStringMatcherRef = new AtomicReference<>(null);

	private final GenreRepository genreRepository;


	GenreMatcherService(GenreRepository genreRepository) {
		this.genreRepository = genreRepository;
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
			.map(getCanonicalStringMatcher()::canonicalize)
			.flatMap(Optional::stream)
			.collect(Collectors.toUnmodifiableSet());

		LOGGER.debug("Matched genres '{}' to '{}'.", unmatchedGenres, matches);

		return matches;
	}


	private CanonicalStringMatcher getCanonicalStringMatcher() {
		/*
		 * It is possible for two threads to concurrently try to initialize the string matcher.
		 * If that happens, the first one wins, with any further initialization still starting but never being applied.
		 */
		if (canonicalStringMatcherRef.get() == null) {
			Set<String> delimiters = Set.of("-", " ", " and ", " & ");

			Collator collator = Collator.getInstance(Locale.ROOT);
			// While PRIMARY may seem fitting here, there are genres that would mistakenly be
			// treated as identical (https://musicbrainz.org/genre/57a6dcc1-c3cd-4ce1-9fb2-e1783992a683 and https://musicbrainz.org/genre/c1f813d2-d21f-4eda-85e3-e8bfac92b3e1).
			collator.setStrength(Collator.SECONDARY);

			Set<String> canonicalGenres = genreRepository.findGenreNames();

			CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalGenres, collator, delimiters);
			canonicalStringMatcherRef.compareAndSet(null, canonicalStringMatcher);
		}
		return canonicalStringMatcherRef.get();
	}
}
