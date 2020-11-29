package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Matcher;

public class RegexUtils {

    private RegexUtils() {
    }

    public static @NotNull Optional<String> maybeGroup(@NotNull Matcher matcher, @NotNull String groupName) {
        return matcher.matches() ? Optional.ofNullable(matcher.group(groupName)) : Optional.empty();
    }
}
