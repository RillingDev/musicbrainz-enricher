package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.api.ScrapingService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.core.genre.GenreMatcherService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


// https://rateyourmusic.com/release/album/nine-inch-nails/the-downward-spiral/
// https://musicbrainz.org/release-group/7c4cab8d-dead-3870-b501-93c90fd0a580
@Service
class RateYourMusicReleaseGroupEnricher implements GenreEnricher {

    private static final Logger logger = LoggerFactory.getLogger(RateYourMusicReleaseGroupEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile("rateyourmusic\\.com");

    private static final Evaluator GENRE_QUERY = QueryParser.parse(".genre");

    private final GenreMatcherService genreMatcherService;
    private final ScrapingService scrapingService;

    RateYourMusicReleaseGroupEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
        this.genreMatcherService = genreMatcherService;
        this.scrapingService = scrapingService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation) {
        return scrapingService.load(relation.getTargetId()).map(this::extractTags).map(genreMatcherService::match).orElse(Set.of());
    }

    private @NotNull Set<String> extractTags(@NotNull Document document) {
        Set<String> tags = new HashSet<>(document.select(GENRE_QUERY).eachText());
        tags.addAll(document.select(GENRE_QUERY).eachText());
        return tags;
    }

    @Override
    public boolean relationSupported(@NotNull RelationWs2 relation) {
        if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType())) {
            return false;
        }
        URL url;
        try {
            url = new URL(relation.getTargetId());
        } catch (MalformedURLException e) {
            logger.warn("Could not parse as URL: '{}'.", relation.getTargetId(), e);
            return false;
        }
        return HOST_REGEX.matcher(url.getHost()).matches();
    }

    @Override
    public @NotNull DataType getDataType() {
        return DataType.RELEASE_GROUP;
    }
}
