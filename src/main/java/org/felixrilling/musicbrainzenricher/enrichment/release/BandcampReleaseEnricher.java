package org.felixrilling.musicbrainzenricher.enrichment.release;

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

/**
 * Uses web scraping because the regular API does not seem to be for hobby devs.
 */
// https://musicbrainz.org/release/6bc5d3f7-3a7f-43fd-8df6-f1b63710e816
// https://vectorlovers.bandcamp.com/album/separation-soundtrack
@Service
class BandcampReleaseEnricher implements GenreEnricher {

    private static final Logger logger = LoggerFactory.getLogger(BandcampReleaseEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile(".+\\.bandcamp\\.com");
    private static final Evaluator TAG_QUERY = QueryParser.parse(".tralbum-tags > a");

    private final GenreMatcherService genreMatcherService;
    private final ScrapingService scrapingService;

    BandcampReleaseEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
        this.genreMatcherService = genreMatcherService;
        this.scrapingService = scrapingService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation) {
        return scrapingService.load(relation.getTargetId()).map(this::extractTags).map(genreMatcherService::match).orElse(Set.of());
    }

    private @NotNull Set<String> extractTags(@NotNull Document document) {
        return new HashSet<>(document.select(TAG_QUERY).eachText());
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
        return DataType.RELEASE;
    }
}
