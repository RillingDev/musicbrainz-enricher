package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Bandcamp fetcher.
 * <p>
 * Uses web scraping because the regular API does not seem to be for hobby devs.
 */
@Service
public class BandcampReleaseEnricher implements GenreReleaseEnricher {

    private static final Logger logger = LoggerFactory.getLogger(BandcampReleaseEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile(".+\\.bandcamp\\.com");

    private final GenreMatcherService genreMatcherService;

    BandcampReleaseEnricher(GenreMatcherService genreMatcherService) {
        this.genreMatcherService = genreMatcherService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull String relationUrl) throws IOException {
        Document document = Jsoup.connect(relationUrl).get();
        Set<String> tagsText = new HashSet<>(extractTags(document));
        return genreMatcherService.match(tagsText);
    }

    private List<String> extractTags(@NotNull Document document) {
        Elements tagsMatches = document.getElementsByClass("tralbum-tags");
        if (tagsMatches.size() != 1) {
            throw new IllegalStateException("Unexpected match size.");
        }
        Element tags = tagsMatches.get(0);
        return tags.getElementsByTag("a").eachText();
    }

    @Override
    public boolean relationFits(@NotNull RelationWs2 relationWs2) {
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
