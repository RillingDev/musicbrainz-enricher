package org.felixrilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MergeUtilsTest {

	@Test
	@DisplayName("Returns empty set for no sets provided.")
	void getMostCommonEmptyForEmptyCollection() {
		Set<String> actual = MergeUtils.getMostCommon(Set.of(), 50);

		assertThat(actual).isEmpty();
	}

	@Test
	@DisplayName("Returns empty set for no set items provided.")
	void getMostCommonEmptyForNoItems() {
		Set<String> set1 = Set.of();

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1), 50);

		assertThat(actual).isEmpty();
	}

	@Test
	@DisplayName("Returns input if only one set is provided.")
	void getMostCommonReturnsItemsAsIsForSingleSet() {
		Set<String> set1 = Set.of("foo", "bar");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1), 2);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar");
	}

	@Test
	@DisplayName("Cuts of items with low count.")
	void getMostCommonCutsLowCount() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("foo");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2), 90);

		assertThat(actual).containsExactlyInAnyOrder("foo");
	}

	@Test
	@DisplayName("Returns union if all items have same count.")
	void getMostCommonUnion() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("fizz");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2), 50);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar", "fizz");
	}

	@Test
	@DisplayName("Returns higher count items.")
	void getMostCommonHigherCount() {
		Set<String> set1 = Set.of("foo", "bar");
		Set<String> set2 = Set.of("foo", "bar", "fizz");
		Set<String> set3 = Set.of("foo");

		Set<String> actual = MergeUtils.getMostCommon(Set.of(set1, set2, set3), 50);

		assertThat(actual).containsExactlyInAnyOrder("foo", "bar");
	}
}
