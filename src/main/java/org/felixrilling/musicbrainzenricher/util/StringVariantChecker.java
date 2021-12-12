package org.felixrilling.musicbrainzenricher.util;

import com.google.common.collect.Sets;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tool allowing checking if two strings are variants of the same word.
 * This is done by using delimiters representing common string variant
 * delimiters in the english language, such as "-" (e.g. "hip-hop" vs "hip hop"),
 * and checking if two words are the same ignoring these delimiters.
 * <p>
 * Note that due to the complexity of language, this tool only covers basic cases.
 */
@ThreadSafe
public class StringVariantChecker {

	private final Set<String> delimiters;
	private final Collator collator;

	/**
	 * Constructor.
	 *
	 * @param delimiters Delimiters to use when checking for variants. May contain e.g. {@code "-"} or {@code " and "}.
	 * @param collator   Collator to use for comparing variants.
	 */
	public StringVariantChecker(@NotNull Set<String> delimiters, @NotNull Collator collator) {
		this.delimiters = Set.copyOf(delimiters);
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
	public boolean isVariant(@NotNull String a, @NotNull String b) {
		if (collator.equals(a, b)) {
			return true;
		}

		return !Sets.intersection(createVariants(a), createVariants(b)).isEmpty();
	}

	private @NotNull Set<String> createVariants(@NotNull String string) {
		Set<String> variants = new TreeSet<>(collator);
		variants.add(string);
		for (String delimiter : delimiters) {
			// FIXME: Does not support mixed variants (e.g. 'hip-hop and foo')
			variants.add(string.replaceAll(delimiter, ""));
		}
		return variants;
	}
}
