package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.ScrapingService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.genre.GenreMatcherService;
import dev.rilling.musicbrainzenricher.enrichment.GenreEnricher;
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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Uses web scraping because the regular API does not seem to be documented.
 */
// https://musicbrainz.org/release/4a7262b6-a64d-4214-ae61-bb16d15d724c
// https://www.junodownload.com/products/indivision-mount-vesuvius-newborn-star/4144821-02/
@Service
@ThreadSafe
class JundownloadReleaseEnricher implements GenreEnricher {

	private static final Logger LOGGER = LoggerFactory.getLogger(JundownloadReleaseEnricher.class);

	private static final Pattern HOST_REGEX = Pattern.compile("www\\.junodownload\\.com");
	private static final Evaluator TAG_QUERY = QueryParser.parse("[itemprop='genre']");

	private final GenreMatcherService genreMatcherService;
	private final ScrapingService scrapingService;

	JundownloadReleaseEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
		this.genreMatcherService = genreMatcherService;
		this.scrapingService = scrapingService;
	}

	@Override

	public Set<String> fetchGenres(RelationWs2 relation) {
		return scrapingService.load(relation.getTargetId())
			.map(this::extractTags)
			.map(genreMatcherService::match)
			.orElse(Set.of());
	}


	private Set<String> extractTags(Document document) {
		return Set.of(document.select(TAG_QUERY).attr("content"));
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
