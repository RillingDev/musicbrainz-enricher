package org.felixrilling.musicbrainzenricher.enrichment.release;

import org.felixrilling.musicbrainzenricher.api.ScrapingService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.core.genre.GenreMatcherService;
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
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses web scraping because having to create an Apple account just to fetch music data is overkill.
 */
// https://musicbrainz.org/release/5bcb2971-fdea-4543-baf2-dd41d8b9a3cd
// https://music.apple.com/us/album/1383304609
@Service
class AppleMusicReleaseEnricher implements GenreEnricher {

    private static final Logger logger = LoggerFactory.getLogger(AppleMusicReleaseEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile("(?:itunes|music)\\.apple\\.com");
    private static final Evaluator TAG_QUERY = QueryParser.parse(".product-meta");
    // Value format: "Genre · Year"
    private static final Pattern META_REGEX = Pattern.compile("(?<genre>.+).·.\\d+");

    private final GenreMatcherService genreMatcherService;
    private final ScrapingService scrapingService;

    AppleMusicReleaseEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
        this.genreMatcherService = genreMatcherService;
        this.scrapingService = scrapingService;
    }

    @Override
    public @NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation) {
        Optional<Document> document = scrapingService.load(relation.getTargetId());
        if (document.isEmpty()) {
            return Set.of();
        }

        // We can only process genres if they are in english.
        if (!hasLocaleLanguage(document.get(), Locale.ENGLISH)) {
            logger.debug("Skipping '{}' because the locale is not supported.", relation.getTargetId());
            return Set.of();
        }

        return genreMatcherService.match(extractTags(document.get()));
    }

    private @NotNull Set<String> extractTags(@NotNull Document document) {
        String metaText = document.select(TAG_QUERY).text();

        Matcher matcher = META_REGEX.matcher(metaText);
        if (!matcher.matches()) {
            logger.warn("Could not match meta text. This might be because we were redirected.");
            return Set.of();
        }
        return Set.of(matcher.group("genre"));
    }

    private boolean hasLocaleLanguage(@NotNull Document document, @NotNull Locale locale) {
        String parsedLocale = document.getElementsByTag("html").attr("lang");

        // We manually extract just the language in order to not have to deal with different locale representations
        // (es-mx in HTML vs es_MX in Java).
        String parsedLanguage;
        if (parsedLocale.contains("-")) {
            parsedLanguage = parsedLocale.substring(0, parsedLocale.indexOf("-"));
        } else {
            parsedLanguage = parsedLocale;
        }

        return parsedLanguage.equals(locale.getLanguage());
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
