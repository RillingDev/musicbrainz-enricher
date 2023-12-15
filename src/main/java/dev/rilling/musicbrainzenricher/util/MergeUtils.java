package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;

import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public final class MergeUtils {
	private MergeUtils() {
	}

	/**
	 * Gets the most common items in the sets.
	 * The more of the sets an item exists in, the higher its count will be.
	 * The higher `minUsagePercentage`, the higher an items count has to be included in the result.
	 *
	 * @param sets               Collection of sets to analyze the contents of.
	 * @param minUsagePercentage Percentage from 0 to 1.
	 *                           If e.g. 0.9 is used, all values with a count of at least 90%
	 *                           of the item of the highest count are included.
	 * @param <T>                Set item value.
	 * @return Set containing items of the original sets with a high count.
	 */

	public static <T> Set<T> getMostCommon( Collection<Set<T>> sets, double minUsagePercentage) {
		if (minUsagePercentage < 0 || minUsagePercentage > 1) {
			throw new IllegalArgumentException("minUsagePercentage must be from 0 to 1.");
		}

		if (sets.isEmpty()) {
			return Set.of();
		}
		if (sets.size() == 1) {
			return Set.copyOf(sets.iterator().next());
		}

		List<T> all = mergeIntoList(sets);
		return getMostCommon(all, minUsagePercentage);
	}


	private static <T> Set<T> getMostCommon(List<T> all, double minUsagePercentage) {
		if (all.isEmpty()) {
			return Set.of();
		}

		// Roughly based on Musicbrainz Picard's picard.track.Track._convert_folksonomy_tags_to_genre
		Map<T, Integer> counted = count(all);
		// Max count must be present as we know the map is not empty.
		int maxCount = counted.values().stream().max(Integer::compareTo).orElseThrow();

		return counted.entrySet().stream().filter(entry -> {
			double usagePercentage = ((double) entry.getValue()) / maxCount;
			return usagePercentage >= minUsagePercentage;
		}).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
	}


	private static <T> Map<T, Integer> count( Collection<T> all) {
		Map<T, Integer> counted = new HashMap<>(all.size());
		for (T t : all) {
			counted.compute(t, (ignored, count) -> count == null ? 1 : count + 1);
		}
		return counted;
	}


	private static <T> List<T> mergeIntoList( Collection<? extends Collection<T>> sets) {
		List<T> all = new ArrayList<>(sets.size() * 5);
		sets.forEach(all::addAll);
		return all;
	}

}
