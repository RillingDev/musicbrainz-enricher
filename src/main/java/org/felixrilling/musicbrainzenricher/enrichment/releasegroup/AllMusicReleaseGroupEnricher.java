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
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * AllMusic does not seem to have an API, so scraping it is.
 */
// https://musicbrainz.org/release-group/a63e5fa6-d6ad-47bd-986d-4a27b0c9de70
// https://www.allmusic.com/album/mw0003185404
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
	@NotNull
	public Set<String> fetchGenres(@NotNull RelationWs2 relation) {
		return scrapingService.load(relation.getTargetId())
			.map(this::extractTags)
			.map(genreMatcherService::match)
			.orElse(Set.of());
	}

	@NotNull
	private Set<String> extractTags(@NotNull Document document) {
		Set<String> tags = new HashSet<>(document.select(GENRE_QUERY).eachText());
		tags.addAll(document.select(STYLES_QUERY).eachText());
		return tags;
	}

	@Override
	public boolean isRelationSupported(@NotNull RelationWs2 relation) {
		return "http://musicbrainz.org/ns/rel-2.0#allmusic".equals(relation.getType()) &&
			"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType());
	}

	@Override
	@NotNull
	public DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}
}
