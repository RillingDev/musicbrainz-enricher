package dev.rilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalStringMatcherTest {

	@Test
	@DisplayName("canonicalizes.")
	void canonicalizeMatch() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop");
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues,
			stringVariantChecker);

		assertThat(canonicalStringMatcher.canonicalize("rock")).contains("rock");
		assertThat(canonicalStringMatcher.canonicalize("hiphop")).contains("hip-hop");
	}

	@Test
	@DisplayName("returns empty for no match.")
	void canonicalizeEmptyForNoMatch() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop");
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "),
			Collator.getInstance(Locale.ROOT));
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues,
			stringVariantChecker);

		assertThat(canonicalStringMatcher.canonicalize("jazz")).isEmpty();
		assertThat(canonicalStringMatcher.canonicalize("hipXhop")).isEmpty();
	}
}
