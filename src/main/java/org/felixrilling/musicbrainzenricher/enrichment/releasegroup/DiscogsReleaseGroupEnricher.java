package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.DataType;
import org.felixrilling.musicbrainzenricher.api.discogs.DiscogsMaster;
import org.felixrilling.musicbrainzenricher.api.discogs.DiscogsQueryService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.enrichment.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class DiscogsReleaseGroupEnricher implements GenreEnricher {

    private static final Pattern URL_REGEX = Pattern.compile("http(?:s?)://www\\.discogs\\.com/master/(?<id>\\d+)");

    private final GenreMatcherService genreMatcherService;
    private final DiscogsQueryService discogsQueryService;

    DiscogsReleaseGroupEnricher(GenreMatcherService genreMatcherService, DiscogsQueryService discogsQueryService) {
        this.genreMatcherService = genreMatcherService;
        this.discogsQueryService = discogsQueryService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull String relationUrl) {
        return discogsQueryService.lookUpMaster(findReleaseId(relationUrl)).map(release -> genreMatcherService.match(extractGenres(release)))
                .orElse(Set.of());
    }


    private @NotNull String findReleaseId(@NotNull String relationUrl) {
        Matcher matcher = URL_REGEX.matcher(relationUrl);
        //noinspection ResultOfMethodCallIgnored We know we matched in #relationFits
        matcher.matches();
        return matcher.group("id");
    }

    private @NotNull Set<String> extractGenres(@NotNull DiscogsMaster master) {
        Set<String> genres = new HashSet<>(master.getGenres());
        if (master.getStyles() != null) {
            genres.addAll(master.getStyles());
        }
        return genres;
    }

    @Override
    public boolean relationSupported(@NotNull RelationWs2 relationWs2) {
        if (!"http://musicbrainz.org/ns/rel-2.0#discogs".equals(relationWs2.getType())) {
            return false;
        }

        String targetUrl = relationWs2.getTargetId();
        return URL_REGEX.matcher(targetUrl).matches();
    }

    @Override
    public boolean dataTypeSupported(@NotNull DataType dataType) {
        return dataType.equals(DataType.RELEASE_GROUP);
    }
}
