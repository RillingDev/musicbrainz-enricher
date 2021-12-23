package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

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

	private static final Comparator<String> DESC_LENGTH_COMP = Comparator.comparing(String::length)
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
	public StringVariantChecker(@NotNull Set<String> delimiters, @NotNull Collator collator) {
		if (delimiters.contains("")) {
			throw new IllegalArgumentException("Empty string is not allowed in delimiters.");
		}
		delimiterPattern = Pattern.compile(delimiters.stream()
			.sorted(DESC_LENGTH_COMP) // Ensure long delimiters are at the start so that e.g " and " matches before " ".
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
	public boolean isVariant(@NotNull String a, @NotNull String b) {
		return collator.equals(normalize(a), normalize(b));
	}

	private @NotNull String normalize(@NotNull String string) {
		return delimiterPattern.matcher(string).replaceAll("");
	}
}
