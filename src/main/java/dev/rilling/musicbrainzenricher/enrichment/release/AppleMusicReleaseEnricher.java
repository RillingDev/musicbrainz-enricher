package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.ScrapingService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.genre.GenreMatcherService;
import dev.rilling.musicbrainzenricher.enrichment.Enricher;
import net.jcip.annotations.ThreadSafe;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses web scraping because having to create an Apple account just to fetch music data is overkill.
 */
// https://musicbrainz.org/release/5bcb2971-fdea-4543-baf2-dd41d8b9a3cd
// https://music.apple.com/us/album/1383304609
@Service
@ThreadSafe
class AppleMusicReleaseEnricher implements Enricher {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppleMusicReleaseEnricher.class);

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
	public Set<String> fetchGenres(RelationWs2 relation) {
		return scrapingService.load(relation.getTargetId())
			.filter(document -> {
				// We can only process genres if they are in english.
				if (!hasLocaleLanguage(document, Locale.ENGLISH)) {
					LOGGER.debug("Skipping '{}' because the locale is not supported.", relation.getTargetId());
					return false;
				}
				return true;
			})
			.map(this::extractTags)
			.map(genreMatcherService::match)
			.orElse(Set.of());
	}


	private Set<String> extractTags(Document document) {
		String metaText = document.select(TAG_QUERY).text();

		Matcher matcher = META_REGEX.matcher(metaText);
		if (!matcher.matches()) {
			LOGGER.warn("Could not match meta text. This may be because we were redirected.");
			return Set.of();
		}
		return Set.of(matcher.group("genre"));
	}

	private boolean hasLocaleLanguage(Document document, Locale locale) {
		String parsedLocale = document.getElementsByTag("html").attr("lang");

		// We manually extract just the language to not have to deal with different locale representations
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
	public boolean isRelationSupported(RelationWs2 relation) {
		if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType())) {
			return false;
		}
		URI url;
		try {
			url = new URI(relation.getTargetId());
		} catch (URISyntaxException e) {
			LOGGER.warn("Could not parse as URL: '{}'.", relation.getTargetId(), e);
			return false;
		}
		return HOST_REGEX.matcher(url.getHost()).matches();
	}


	@Override

	public DataType getDataType() {
		return DataType.RELEASE;
	}
}
