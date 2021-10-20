package org.felixrilling.musicbrainzenricher.util;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tool allowing checking if two strings are variants of the same word.
 * This is done by using delimiters representing common string variant
 * delimiters in the english language, such as "-" (e.g. "hip-hop" vs "hip hop"),
 * and checking if two words are the same ignoring these delimiters.
 * String case is ignored.
 * Note that due to the complexity of language, this tool only covers basic cases.
 * <p>
 * As instances of this class are de-facto stateless, thread safety is provided.
 */
public class StringVariantChecker {

	private final Set<String> delimiters;

	public StringVariantChecker(@NotNull Set<String> delimiters) {
		this.delimiters = Set.copyOf(delimiters);
	}

	public boolean isVariant(@NotNull String a, @NotNull String b) {
		Collection<String> intersection = CollectionUtils.intersection(createVariants(a), createVariants(b));
		return !intersection.isEmpty();
	}

	private Set<String> createVariants(@NotNull String string) {
		// FIXME: Does not support mixed variants (e.g. 'hip-hop and foo')
		return delimiters.stream()
			.map(delimiter -> string.toLowerCase(Locale.ROOT).replaceAll(delimiter, ""))
			.collect(Collectors.toSet());
	}
}
