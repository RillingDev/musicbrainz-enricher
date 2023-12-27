package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allows matching a string to its canonical form.
 * <p>
 * This is done by using a configurable {@link Collator} as well as a collection of substrings
 * representing common string variant delimiters in the english language, such as "-" (e.g., "hip-hop" vs "hip hop"),
 * and checking if two words are the same ignoring these delimiters.
 * <p>
 * Note that due to the complexity of language, this tool only covers basic cases.
 */
@ThreadSafe
public class CanonicalStringMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(CanonicalStringMatcher.class);

	private final Pattern ignoredSubstringPattern;
	private final Map<String, String> canonicalMap;

	/**
	 * Constructor.
	 *
	 * @param canonicalValues   Canonical values that should be matched towards.
	 * @param collator          Collator to use when comparing values.
	 * @param ignoredSubstrings Substrings that should be ignored while matching.
	 *                          For example, this can be used to treat {@code " and "} the same as {@code " & "}.
	 *                          Caution: these should be generic substrings that could be used interchangeably.
	 */
	public CanonicalStringMatcher(Set<String> canonicalValues,
								  Collator collator, Set<String> ignoredSubstrings
	) {
		ignoredSubstringPattern = Pattern.compile(ignoredSubstrings.stream()
			// Ensure long substrings are at the start so that for example " and " matches before " ".
			.sorted(Comparator.comparing(String::length).reversed().thenComparing(Comparator.naturalOrder()))
			.map(Pattern::quote)
			.collect(Collectors.joining("|")));

		// Using a tree map with the collator and the adjusted canonical value as key makes for fast lookups.
		canonicalMap = new TreeMap<>(collator);
		for (String canonicalValue : canonicalValues) {
			canonicalMap.put(removeIgnoredSubstrings(canonicalValue), canonicalValue);
		}
	}

	/**
	 * Attempt to get the canonical form
	 *
	 * @param unmatchedValue Value to get the canonical form of.
	 * @return Canonical form, or empty if no canonical match was found.
	 */

	public Optional<String> canonicalize(String unmatchedValue) {
		return Optional.ofNullable(canonicalMap.get(removeIgnoredSubstrings(unmatchedValue)));
	}

	private String removeIgnoredSubstrings(String string) {
		return ignoredSubstringPattern.matcher(string).replaceAll("");
	}
}
