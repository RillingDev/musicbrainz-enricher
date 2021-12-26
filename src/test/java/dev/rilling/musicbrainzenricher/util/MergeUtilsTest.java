package dev.rilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MergeUtilsTest {

	@Test
	@DisplayName("returns empty set for no sets provided.")
	void getMostCommonEmptyForEmptyCollection() {
		Set<String> actual = MergeUtils.getMostCommon(Set.of(), 0.5);

		assertThat(actual).isEmpty();
	}

	@Test
	@DisplayName("returns empty set for no set items provided.")
	void getMostCommonEmptyForNoItems() {
		Set<String> set1 = Set.of();

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1), 0.5);

		assertThat(actual).isEmpty();
	}

	@Test
	@DisplayName("returns input if only one set is provided.")
	void getMostCommonReturnsItemsAsIsForSingleSet() {
		Set<String> set1 = Set.of("foo", "bar");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1), 0.02);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar");
	}

	@Test
	@DisplayName("cuts of items with low count.")
	void getMostCommonCutsLowCount() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("foo");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2), 0.9);

		assertThat(actual).containsExactlyInAnyOrder("foo");
	}

	@Test
	@DisplayName("returns union if all items have same count.")
	void getMostCommonUnion() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("fizz");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2), 0.5);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar", "fizz");
	}

	@Test
	@DisplayName("returns higher count items.")
	void getMostCommonHigherCount() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("foo", "bar", "fizz");
		Set<String> set3 = Set.of("foo");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2, set3), 0.5);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar");
	}
}
