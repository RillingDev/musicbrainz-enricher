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
 * Uses web scraping because the regular API does not seem to be for hobby devs.
 */
// https://musicbrainz.org/release/6bc5d3f7-3a7f-43fd-8df6-f1b63710e816
// https://vectorlovers.bandcamp.com/album/separation-soundtrack
@Service
@ThreadSafe
class BandcampReleaseEnricher implements GenreEnricher {

	private static final Logger LOGGER = LoggerFactory.getLogger(BandcampReleaseEnricher.class);

	private static final Pattern HOST_REGEX = Pattern.compile(".+\\.bandcamp\\.com");
	private static final Evaluator TAG_QUERY = QueryParser.parse(".tralbum-tags > .tag");

	private final GenreMatcherService genreMatcherService;
	private final ScrapingService scrapingService;

	BandcampReleaseEnricher(GenreMatcherService genreMatcherService, ScrapingService scrapingService) {
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
		return Set.copyOf(document.select(TAG_QUERY).eachText());
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
