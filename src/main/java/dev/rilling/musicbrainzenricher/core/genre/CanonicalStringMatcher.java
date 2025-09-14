package dev.rilling.musicbrainzenricher.core.genre;

import net.jcip.annotations.ThreadSafe;

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
class CanonicalStringMatcher {
	private final Pattern ignoredSubstringPattern;
	private final Map<String, String> canonicalMap;
	private final Map<String, String> normalizedCanonicalMap;

	/**
	 * Constructor.
	 *
	 * @param canonicalValues   Canonical values that should be matched towards.
	 * @param collator          Collator to use when comparing values.
	 *                          Caution: The caller should make sure none of the canonical values are equal to each other with this collator.
	 * @param ignoredSubstrings Substrings that should be ignored while matching.
	 *                          For example, this can be used to treat {@code " and "} the same as {@code " & "}.
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

		canonicalMap = new TreeMap<>(collator);
		normalizedCanonicalMap = new TreeMap<>(collator);

		Set<String> collidingKeys = new HashSet<>();
		for (String canonicalValue : canonicalValues) {
			// Having a value->value map may seem silly, but it is useful for applying the collator.
			canonicalMap.put(canonicalValue, canonicalValue);

			String normalizedValue = normalize(canonicalValue);
			if (normalizedCanonicalMap.containsKey(normalizedValue)) {
				collidingKeys.add(normalizedValue);
				collidingKeys.add(canonicalValue);
				continue;
			}
			normalizedCanonicalMap.put(normalizedValue, canonicalValue);
		}
		// If keys collide, we opt out of the normalized resolving, as it pick whatever was inserted first
		collidingKeys.forEach(normalizedCanonicalMap::remove);
	}

	/**
	 * Attempt to get the canonical form
	 *
	 * @param unmatchedValue Value to get the canonical form of.
	 * @return Canonical form, or empty if no canonical match was found.
	 */

	public Optional<String> canonicalize(String unmatchedValue) {
		// We first search for exact matches.
		// This helps avoid situations where multiple canonical values have colliding replaced values.
		if (canonicalMap.containsKey(unmatchedValue)) {
			return Optional.of(canonicalMap.get(unmatchedValue));
		}
		return Optional.ofNullable(normalizedCanonicalMap.get(normalize(unmatchedValue)));
	}

	private String normalize(String string) {
		return ignoredSubstringPattern.matcher(string).replaceAll("");
	}
}
