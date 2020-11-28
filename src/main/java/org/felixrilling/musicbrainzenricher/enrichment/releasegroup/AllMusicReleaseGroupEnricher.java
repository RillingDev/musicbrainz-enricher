package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.DataType;
import org.felixrilling.musicbrainzenricher.api.ScrapingService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.enrichment.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * AllMusic does not seem to have an API, so scraping it is.
 */
// https://www.allmusic.com/album/mw0000087254
@Service
class AllMusicReleaseGroupEnricher implements GenreEnricher {

    private static final Evaluator GENRE_QUERY = QueryParser.parse(".genre > div > a");
    private static final Evaluator STYLES_QUERY = QueryParser.parse(".styles > div > a");

    private final GenreMatcherService genreMatcherService;
    private final ScrapingService scrapingService;

    AllMusicReleaseGroupEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
        this.genreMatcherService = genreMatcherService;
        this.scrapingService = scrapingService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation) {
        return scrapingService.load(relation.getTargetId()).map(this::extractTags).map(genreMatcherService::match).orElse(Set.of());
    }

    private @NotNull Set<String> extractTags(@NotNull Document document) {
        Set<String> tags = new HashSet<>(document.select(GENRE_QUERY).eachText());
        tags.addAll(document.select(STYLES_QUERY).eachText());
        return tags;
    }

    @Override
    public boolean relationSupported(@NotNull RelationWs2 relation) {
        return "http://musicbrainz.org/ns/rel-2.0#allmusic".equals(relation.getType()) && "http://musicbrainz.org/ns/rel-2.0#url"
                .equals(relation.getTargetType());
    }

    @Override
    public @NotNull DataType getDataType() {
        return DataType.RELEASE_GROUP;
    }
}
