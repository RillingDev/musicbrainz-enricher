package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.api.discogs.DiscogsQueryService;
import org.felixrilling.musicbrainzenricher.api.discogs.DiscogsRelease;
import org.felixrilling.musicbrainzenricher.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiscogsReleaseEnricher implements GenreReleaseEnricher {

    private static final Pattern URL_REGEX = Pattern.compile("http(?:s?)://www\\.discogs\\.com/release/(?<id>\\d+)");

    private final GenreMatcherService genreMatcherService;
    private final DiscogsQueryService discogsQueryService;

    DiscogsReleaseEnricher(GenreMatcherService genreMatcherService, DiscogsQueryService discogsQueryService) {
        this.genreMatcherService = genreMatcherService;
        this.discogsQueryService = discogsQueryService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull String relationUrl) {
        return discogsQueryService.lookUpRelease(findReleaseId(relationUrl)).map(release -> genreMatcherService.match(extractGenres(release)))
                .orElse(Set.of());
    }


    private @NotNull String findReleaseId(@NotNull String relationUrl) {
        Matcher matcher = URL_REGEX.matcher(relationUrl);
        //noinspection ResultOfMethodCallIgnored We know we matched in #relationFits
        matcher.matches();
        return matcher.group("id");
    }

    private @NotNull Set<String> extractGenres(@NotNull DiscogsRelease release) {
        Set<String> genres = new HashSet<>(release.getGenres());
        if (release.getStyles() != null) {
            genres.addAll(release.getStyles());
        }
        return genres;
    }

    @Override
    public boolean relationFits(@NotNull RelationWs2 relationWs2) {
        if (!"http://musicbrainz.org/ns/rel-2.0#discogs".equals(relationWs2.getType())) {
            return false;
        }

        String targetUrl = relationWs2.getTargetId();
        return URL_REGEX.matcher(targetUrl).matches();
    }
}
