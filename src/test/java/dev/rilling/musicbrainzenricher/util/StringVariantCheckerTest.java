package dev.rilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StringVariantCheckerTest {

	@Test
	@DisplayName("returns true when comparing against self")
	void isVariantTrueForSelf() {
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));

		assertThat(stringVariantChecker.isVariant("hip-hop", "hip-hop")).isTrue();
	}

	@Test
	@DisplayName("detects variants")
	void isVariantDetectsVariants() {
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));

		assertThat(stringVariantChecker.isVariant("hip-hop", "hip hop")).isTrue();
		assertThat(stringVariantChecker.isVariant("hip hop", "hip-hop")).isTrue();
		assertThat(stringVariantChecker.isVariant("hiphop", "hip hop")).isTrue();
		assertThat(stringVariantChecker.isVariant("hip---hop", "hiphop")).isTrue();

		assertThat(stringVariantChecker.isVariant("hophip", "hiphop")).isFalse();
	}

	@Test
	@DisplayName("only uses specified delimiters")
	void isVariantUsesspecifiedDelimiters() {
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));

		assertThat(stringVariantChecker.isVariant("hiphop", "hipXhop")).isFalse();
		assertThat(stringVariantChecker.isVariant("hip-hop", "hipXhop")).isFalse();
	}

	@Test
	@DisplayName("detects variants of mixed delimiters")
	void isVariantDetectsVariantsMixed() {
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));

		assertThat(stringVariantChecker.isVariant("hiphop-music", "hip-hop music")).isTrue();
		assertThat(stringVariantChecker.isVariant("hiphop- music", "hip-hop music")).isTrue();
	}

	@Test
	@DisplayName("detects variants with delimiters containing each other")
	void isVariantDetectsVariantsContained() {
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of(" and ", " & ", " "),
			Collator.getInstance(Locale.ROOT));

		assertThat(stringVariantChecker.isVariant("drum and bass", "drum and bass")).isTrue();
		assertThat(stringVariantChecker.isVariant("drum and bass", "drum & bass")).isTrue();
		assertThat(stringVariantChecker.isVariant("drum and bass", "drum bass")).isTrue();
	}

	@Test
	@DisplayName("respects collator")
	void isVariantRespectsCollator() {
		Collator spanishCollator = Collator.getInstance(Locale.forLanguageTag("es"));
		spanishCollator.setStrength(Collator.PRIMARY);
		// https://musicbrainz.org/genres
		assertThat(new StringVariantChecker(Set.of("-", " "), spanishCollator).isVariant("yé-yé", "ye ye")).isTrue();

		Collator caseInsensitiveCollator = Collator.getInstance(Locale.ROOT);
		caseInsensitiveCollator.setStrength(Collator.SECONDARY);
		assertThat(new StringVariantChecker(Set.of("-", " "), caseInsensitiveCollator).isVariant("Hip Hop",
			"hip hop")).isTrue();

		Collator caseSensitiveCollator = Collator.getInstance(Locale.ROOT);
		caseSensitiveCollator.setStrength(Collator.TERTIARY);
		assertThat(new StringVariantChecker(Set.of("-", " "), caseSensitiveCollator).isVariant("Hip Hop",
			"hip hop")).isFalse();
	}
}
