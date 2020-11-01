package org.felixrilling.musicbrainzenricher.release;

import org.felixrilling.musicbrainzenricher.genre.GenreMatcherService;
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
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Discogs fetcher.
 */
@Service
public class DiscogsReleaseEnricher implements GenreReleaseEnricher {

    private static final Logger logger = LoggerFactory.getLogger(DiscogsReleaseEnricher.class);

    private static final Pattern HOST_REGEX = Pattern.compile("www\\.discogs\\.com");

    private static final Pattern GENRE_LINK_REGEX = Pattern.compile("/genre/(.*)");
    private static final Pattern STYLE_LINK_REGEX = Pattern.compile("/style/(.*)");

    private final GenreMatcherService genreMatcherService;

    DiscogsReleaseEnricher(GenreMatcherService genreMatcherService) {
        this.genreMatcherService = genreMatcherService;
    }

    @Override
    public Set<String> fetchGenres(String relationUrl) throws IOException {
        Document document = Jsoup.connect(relationUrl).get();
        return genreMatcherService.match(extractTags(document));
    }

    private Set<String> extractTags(Document document) {
        Elements profileMatches = document.getElementsByClass("profile");
        if (profileMatches.size() != 1) {
            throw new IllegalStateException("Unexpected match size.");
        }
        Element profile = profileMatches.get(0);

        Set<String> genreLikes = new HashSet<>();
        genreLikes.addAll(extractGenreLike(profile.getElementsByAttributeValueStarting("href", "/genre/"), GENRE_LINK_REGEX));
        genreLikes.addAll(extractGenreLike(profile.getElementsByAttributeValueStarting("href", "/style/"), STYLE_LINK_REGEX));
        return genreLikes;
    }

    private Set<String> extractGenreLike(Elements genreLikeLinks, Pattern genreLinkRegex) {
        Set<String> genreLikes = new HashSet<>();
        for (Element genreLikeLink : genreLikeLinks) {
            String href = genreLikeLink.attr("href");
            Matcher matcher = genreLinkRegex.matcher(href);
            if (!matcher.matches()) {
                logger.warn("Could not find genre-like value in URL: '{}'.", href);
                continue;
            }

            /*
             * The genre-like links have their last path extracted by regex,
             * Then any query params dropped and finally their value decoded.
             */
            String extracted = matcher.group(1);
            String path = URI.create(extracted).getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

            genreLikes.add(decodedPath);
        }
        return genreLikes;
    }

    @Override
    public boolean relationFits(RelationWs2 relationWs2) {
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
