package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Uses web scraping because the regular API does not seem to be documented.
 */
// https://musicbrainz.org/release/4a7262b6-a64d-4214-ae61-bb16d15d724c
// https://www.junodownload.com/products/indivision-mount-vesuvius-newborn-star/4144821-02/
@Service
public class JundownloadReleaseEnricher implements GenreReleaseEnricher {

    private static final Logger logger = LoggerFactory.getLogger(JundownloadReleaseEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile("www\\.junodownload\\.com");
    private static final Evaluator TAG_QUERY = QueryParser.parse("[itemprop='genre']");

    private final GenreMatcherService genreMatcherService;

    JundownloadReleaseEnricher(GenreMatcherService genreMatcherService) {
        this.genreMatcherService = genreMatcherService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull String relationUrl) {
        Document document;
        try {
            document = Jsoup.connect(relationUrl).get();
        } catch (IOException e) {
            logger.warn("Could not connect to '{}', skipping it.", relationUrl);
            return Set.of();
        }
        return genreMatcherService.match(extractTags(document));
    }

    private @NotNull Set<String> extractTags(@NotNull Document document) {
        return Set.of(document.select(TAG_QUERY).attr("content"));
    }

    @Override
    public boolean relationFits(@NotNull RelationWs2 relationWs2) {
        if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relationWs2.getTargetType())) {
            return false;
        }
        URL url;
        try {
            url = new URL(relationWs2.getTargetId());
        } catch (MalformedURLException e) {
            logger.warn("Could not parse as URL: '{}'.", relationWs2.getTargetId());
            return false;
        }
        return HOST_REGEX.matcher(url.getHost()).matches();
    }
}
