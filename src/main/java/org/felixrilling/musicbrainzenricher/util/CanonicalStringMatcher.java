package org.felixrilling.musicbrainzenricher.util;

import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Allows matching a string to its canonical form.
 * A given value will be checked against the provided canonical values using a {@link StringVariantChecker}.
 *
 * @see StringVariantChecker
 */
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
	 * @param stringVariantChecker {@link StringVariantChecker} to use for matching.
	 */
	public CanonicalStringMatcher(Set<String> canonicalValues, StringVariantChecker stringVariantChecker) {
		this.stringVariantChecker = stringVariantChecker;
		this.canonicalValues = Set.copyOf(canonicalValues);

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
}
