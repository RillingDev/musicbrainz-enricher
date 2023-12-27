package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allows matching a string to its canonical form.
 * A given value will be checked against the provided canonical values using a {@link StringVariantChecker}.
 *
 * @see StringVariantChecker
 */
@ThreadSafe
public class CanonicalStringMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalStringMatcher.class);
	private static final int SIZE_FACTOR = 5;

	private final StringVariantChecker stringVariantChecker;
	private final Set<String> canonicalValues;
	private final Map<String, Optional<String>> cache;

	/**
	 * Constructor.
	 *
	 * @param canonicalValues      Canonical values that should be matched towards.
	 */
	public CanonicalStringMatcher(Set<String> canonicalValues,
								  Set<String> delimiters, Collator collator) {
		this.stringVariantChecker = new StringVariantChecker(delimiters, collator);
		this.canonicalValues = Set.copyOf(canonicalValues);

		// TODO: check if a bounded version of ConcurrentHashMap can be used instead
		cache = Collections.synchronizedMap(new LRUMap<>(canonicalValues.size() * SIZE_FACTOR));
	}

	/**
	 * Attempt to get the canonical form
	 *
	 * @param unmatchedValue Value to get the canonical form of.
	 * @return Canonical form, or empty if no canonical match was found.
	 */

	public Optional<String> canonicalize(String unmatchedValue) {
		return cache.computeIfAbsent(unmatchedValue, value -> {
			Optional<String> match = canonicalValues.stream()
				.filter(canonicalValue -> stringVariantChecker.isVariant(value, canonicalValue))
				.findFirst();
			LOGGER.trace("Matched '{}' to '{}'.", value, match);
			return match;
		});
	}

	/**
	 * Tool allowing checking if two strings are variants of the same word.
	 * This is done by using delimiters representing common string variant
	 * delimiters in the english language, such as "-" (e.g. "hip-hop" vs "hip hop"),
	 * and checking if two words are the same ignoring these delimiters.
	 * <p>
	 * Note that due to the complexity of language, this tool only covers basic cases.
	 */
	@ThreadSafe
	static
	class StringVariantChecker {

		private static final Comparator<String> DESCENDING_LENGTH_COMPARATOR = Comparator.comparing(String::length)
			.reversed()
			.thenComparing(Comparator.naturalOrder());

		private final Collator collator;
		private final Pattern delimiterPattern;

		/**
		 * Constructor.
		 *
		 * @param delimiters Delimiters to use when checking for variants. E.g. {@code "-"} or {@code " and "}.
		 * @param collator   Collator to use for comparing variants.
		 */
		public StringVariantChecker(Set<String> delimiters, Collator collator) {
			if (delimiters.contains("")) {
				throw new IllegalArgumentException("Empty string is not allowed in delimiters.");
			}
			delimiterPattern = Pattern.compile(delimiters.stream()
				// Ensure long delimiters are at the start so that e.g " and " matches before " ".
				.sorted(DESCENDING_LENGTH_COMPARATOR)
				.map(Pattern::quote)
				.collect(Collectors.joining("|")));
			this.collator = collator;
		}

		/**
		 * Checks if a and b are variants of each other.
		 * Order of parameters does not affect the result.
		 *
		 * @param a Value a.
		 * @param b Value b.
		 * @return if a and b are variants of each other.
		 */
		public boolean isVariant(String a, String b) {
			return collator.equals(normalize(a), normalize(b));
		}

		private String normalize(String string) {
			return delimiterPattern.matcher(string).replaceAll("");
		}
	}
}
