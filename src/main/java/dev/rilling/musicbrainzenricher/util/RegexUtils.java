package dev.rilling.musicbrainzenricher.util;

import net.jcip.annotations.ThreadSafe;

import java.util.Optional;
import java.util.regex.Matcher;

@ThreadSafe
public final class RegexUtils {

	private RegexUtils() {
	}


	public static Optional<String> maybeGroup(Matcher matcher, String groupName) {
		return matcher.matches() ? Optional.ofNullable(matcher.group(groupName)) : Optional.empty();
	}
}
