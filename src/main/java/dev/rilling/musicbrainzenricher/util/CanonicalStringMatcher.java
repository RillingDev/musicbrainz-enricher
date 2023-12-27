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
	 *                          Caution: The caller should make sure none of the canonical values are equal to each other with this collator.
	 * @param ignoredSubstrings Substrings that should be ignored while matching.
	 *                          For example, this can be used to treat {@code " and "} the same as {@code " & "}.
	 *                          Caution: The caller should make sure none of the canonical values are equal to each other when ignoring these substrings.
	 *                          Caution: These should be generic substrings that could be used interchangeably.
	 */
	public CanonicalStringMatcher(Set<String> canonicalValues,
								  Collator collator, Set<String> ignoredSubstrings
	) {
		ignoredSubstringPattern = Pattern.compile(ignoredSubstrings.stream()
			// Ensure long substrings are at the start so that for example " and " matches before " ".
			.sorted(Comparator.comparing(String::length).reversed().thenComparing(Comparator.naturalOrder()))
			.map(Pattern::quote)
			.collect(Collectors.joining("|")));

		List<String> list = canonicalValues.stream().map(this::removeIgnoredSubstrings).toList();


		// Using a tree map with the collator and the adjusted canonical value as key makes for fast lookups.
		canonicalMap = new TreeMap<>(collator);
		for (String canonicalValue : canonicalValues) {
			String adjustedValue = removeIgnoredSubstrings(canonicalValue);
			if (canonicalMap.containsKey(adjustedValue)) {
				LOGGER.warn("Canonical value '{}' conflicts with '{}' which is equal when comparing.", canonicalValue, canonicalMap.get(adjustedValue));
			}
			canonicalMap.put(adjustedValue, canonicalValue);
		}
	}

	/**
	 * Attempt to get the canonical form
	 *
	 * @param unmatchedValue Value to get the canonical form of.
	 * @return Canonical form, or empty if no canonical match was found.
	 */

	public Optional<String> canonicalize(String unmatchedValue) {
		String adjustedValue = removeIgnoredSubstrings(unmatchedValue);
		return Optional.ofNullable(canonicalMap.get(adjustedValue));
	}

	private String removeIgnoredSubstrings(String string) {
		return ignoredSubstringPattern.matcher(string).replaceAll("");
	}
}
