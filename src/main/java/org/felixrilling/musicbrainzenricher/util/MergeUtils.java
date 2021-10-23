package org.felixrilling.musicbrainzenricher.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class MergeUtils {
	private MergeUtils() {
	}

	/**
	 * Gets the most common items in the sets.
	 * The more of the sets an item exists in, the higher its count will be.
	 * The higher `p`, the higher an items count has to be to be included in the result.
	 *
	 * @param sets Collection of sets to analyze the contents of.
	 * @param p    Percentile to calculate the required count by. {@link DescriptiveStatistics#getPercentile(double)}
	 * @param <T>  Set item value.
	 * @return Set containing items of the original sets with a high count.
	 */
	// Oh boy this method is messy. Doesn't help that I don't know much about statistics.
	@NotNull
	public static <T> Set<T> getMostCommon(@NotNull Collection<Set<T>> sets, double p) {
		if (sets.isEmpty()) {
			return Set.of();
		}

		List<T> all = mergeIntoList(sets);
		if (all.isEmpty()) {
			return Set.of();
		}

		Map<T, Integer> counted = count(all);

		DescriptiveStatistics summaryStatistics = new DescriptiveStatistics();
		counted.values().forEach(summaryStatistics::addValue);
		double lowestAllowedCount = summaryStatistics.getPercentile(p);

		return counted.entrySet()
			.stream()
			.filter(entry -> entry.getValue() >= lowestAllowedCount)
			.map(Map.Entry::getKey)
			.collect(Collectors.toUnmodifiableSet());
	}

	@NotNull
	private static <T> Map<T, Integer> count(@NotNull Collection<T> all) {
		Map<T, Integer> counted = new HashMap<>(all.size());
		for (T t : all) {
			counted.compute(t, (ignored, count) -> count == null ? 1 : count + 1);
		}
		return counted;
	}

	@NotNull
	private static <T> List<T> mergeIntoList(@NotNull Collection<? extends Collection<T>> sets) {
		List<T> all = new ArrayList<>(sets.size() * 5);
		sets.forEach(all::addAll);
		return all;
	}

}
