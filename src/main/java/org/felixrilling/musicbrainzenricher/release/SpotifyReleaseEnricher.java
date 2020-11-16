package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.genre.GenreMatcherService;
import org.felixrilling.musicbrainzenricher.io.spotify.SpotifyQueryService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpotifyReleaseEnricher implements GenreReleaseEnricher {

    private static final Pattern URL_REGEX = Pattern.compile("http(?:s?)://open\\.spotify\\.com/album/(?<id>\\w+)");

    private final GenreMatcherService genreMatcherService;
    private final SpotifyQueryService spotifyQueryService;

    SpotifyReleaseEnricher(GenreMatcherService genreMatcherService, SpotifyQueryService spotifyQueryService) {
        this.genreMatcherService = genreMatcherService;
        this.spotifyQueryService = spotifyQueryService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull String relationUrl) {
        return spotifyQueryService.lookUpRelease(findReleaseId(relationUrl)).map(release -> {
            HashSet<String> genres = new HashSet<>(Arrays.asList(release.getGenres()));
            return genreMatcherService.match(genres);
        }).orElse(Set.of());
    }


    private @NotNull String findReleaseId(@NotNull String relationUrl) {
        Matcher matcher = URL_REGEX.matcher(relationUrl);
        //noinspection ResultOfMethodCallIgnored We know we matched in #relationFits
        matcher.matches();
        return matcher.group("id");
    }

    @Override
    public boolean relationFits(@NotNull RelationWs2 relationWs2) {
        if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relationWs2.getTargetType())) {
            return false;
        }

        String targetUrl = relationWs2.getTargetId();
        return URL_REGEX.matcher(targetUrl).matches();
    }
}
