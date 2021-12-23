package dev.rilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalStringMatcherTest {

	@Test
	@DisplayName("canonicalizes")
	void canonicalizes() {
		Set<String> canonicalValues = Set.of("hardcore", "bardcore", "hip-hop");
		StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-"),
			Collator.getInstance(Locale.ROOT));
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues,
			stringVariantChecker);

		assertThat(canonicalStringMatcher.canonicalize("bardcore")).contains("bardcore");
		assertThat(canonicalStringMatcher.canonicalize("hard-core")).contains("hardcore");
		assertThat(canonicalStringMatcher.canonicalize("foo")).isEmpty();
	}
}
