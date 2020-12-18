package org.felixrilling.musicbrainzenricher.enrichment.release;

import org.felixrilling.musicbrainzenricher.api.spotify.SpotifyQueryService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.core.genre.GenreMatcherService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://musicbrainz.org/release/5bcb2971-fdea-4543-baf2-dd41d8b9a3cd
// https://open.spotify.com/album/0Q2o6ioxIOlKPvRdG1K5da
@Service
class SpotifyReleaseEnricher implements GenreEnricher {

    private static final Pattern URL_REGEX = Pattern.compile("http(?:s?)://open\\.spotify\\.com/album/(?<id>\\w+)");

    private final GenreMatcherService genreMatcherService;
    private final SpotifyQueryService spotifyQueryService;

    SpotifyReleaseEnricher(GenreMatcherService genreMatcherService, SpotifyQueryService spotifyQueryService) {
        this.genreMatcherService = genreMatcherService;
        this.spotifyQueryService = spotifyQueryService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation) {
        return spotifyQueryService.lookUpRelease(findReleaseId(relation.getTargetId())).map(release -> {
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
    public boolean relationSupported(@NotNull RelationWs2 relation) {
        if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType())) {
            return false;
        }

        String targetUrl = relation.getTargetId();
        return URL_REGEX.matcher(targetUrl).matches();
    }

    @Override
    public @NotNull DataType getDataType() {
        return DataType.RELEASE;
    }
}
