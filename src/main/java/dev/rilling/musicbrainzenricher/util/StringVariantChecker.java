package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;

import java.text.Collator;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
