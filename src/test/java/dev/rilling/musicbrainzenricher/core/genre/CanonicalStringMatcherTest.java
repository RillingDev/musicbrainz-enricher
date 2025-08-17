package dev.rilling.musicbrainzenricher.core.genre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalStringMatcherTest {

	@Test
	@DisplayName("returns same if already canonical.")
	void returnsSame() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop");
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues, Collator.getInstance(Locale.ROOT), Set.of("-", " "));

		assertThat(canonicalStringMatcher.canonicalize("rock")).contains("rock");
		assertThat(canonicalStringMatcher.canonicalize("hip-hop")).contains("hip-hop");
	}

	@Test
	@DisplayName("returns empty none for if no matching entry exists.")
	void returnsEmpty() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop");
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues, Collator.getInstance(Locale.ROOT), Set.of("-", " "));

		assertThat(canonicalStringMatcher.canonicalize("fizz")).isEmpty();
		assertThat(canonicalStringMatcher.canonicalize("hip-hopper")).isEmpty();
		assertThat(canonicalStringMatcher.canonicalize("hipXhop")).isEmpty();
		assertThat(canonicalStringMatcher.canonicalize("hop hip")).isEmpty();
	}

	@Test
	@DisplayName("canonicalizes.")
	void canonicalizes() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop", "drum & bass");
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues, Collator.getInstance(Locale.ROOT), Set.of("-", " ", " & ", " and "));

		assertThat(canonicalStringMatcher.canonicalize("hip hop")).contains("hip-hop");
		assertThat(canonicalStringMatcher.canonicalize("drum and bass")).contains("drum & bass");
	}

	@Test
	@DisplayName("canonicalizes with mixed separators.")
	void canonicalizeMixed() {
		Set<String> canonicalValues = Set.of("rock", "hip-hop", "super-duper fancy and cool");
		CanonicalStringMatcher canonicalStringMatcher = new CanonicalStringMatcher(canonicalValues, Collator.getInstance(Locale.ROOT), Set.of("-", " ", " & ", " and "));

		assertThat(canonicalStringMatcher.canonicalize("super duper fancy & cool")).contains("super-duper fancy and cool");
	}


	@Test
	@DisplayName("respects collator.")
	void respectsCollator() {
		Set<String> ignoredSubstrings = Set.of("-", " ");

		Collator spanishCollator = Collator.getInstance(Locale.forLanguageTag("es"));
		spanishCollator.setStrength(Collator.PRIMARY);
		assertThat(new CanonicalStringMatcher(Set.of("yé-yé"), spanishCollator, ignoredSubstrings).canonicalize("ye ye")).contains("yé-yé");

		Collator caseInsensitiveCollator = Collator.getInstance(Locale.ROOT);
		caseInsensitiveCollator.setStrength(Collator.SECONDARY);
		assertThat(new CanonicalStringMatcher(Set.of("hip hop"), caseInsensitiveCollator, ignoredSubstrings).canonicalize("Hip Hop")).contains("hip hop");

		Collator caseSensitiveCollator = Collator.getInstance(Locale.ROOT);
		caseSensitiveCollator.setStrength(Collator.TERTIARY);
		assertThat(new CanonicalStringMatcher(Set.of("hip hop"), caseSensitiveCollator, ignoredSubstrings).canonicalize("Hip Hop")).isEmpty();
	}

}
