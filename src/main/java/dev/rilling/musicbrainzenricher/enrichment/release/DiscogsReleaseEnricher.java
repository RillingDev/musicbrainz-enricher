package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.discogs.DiscogsQueryService;
import dev.rilling.musicbrainzenricher.api.discogs.DiscogsRelease;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.genre.GenreMatcherService;
import dev.rilling.musicbrainzenricher.enrichment.GenreEnricher;
import dev.rilling.musicbrainzenricher.util.RegexUtils;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

// https://musicbrainz.org/release/bd3d34dd-218a-4296-afde-b3fa2c39ba29
// https://www.discogs.com/release/12168718
@Service
@ThreadSafe
class DiscogsReleaseEnricher implements GenreEnricher {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscogsReleaseEnricher.class);

	private static final Pattern URL_REGEX = Pattern.compile("https?://www\\.discogs\\.com/release/(?<id>\\d+)");

	private final GenreMatcherService genreMatcherService;
	private final DiscogsQueryService discogsQueryService;

	DiscogsReleaseEnricher(GenreMatcherService genreMatcherService, DiscogsQueryService discogsQueryService) {
		this.genreMatcherService = genreMatcherService;
		this.discogsQueryService = discogsQueryService;
	}

	@Override

	public Set<String> fetchGenres(RelationWs2 relation) {
		Optional<String> discogsId = RegexUtils.maybeGroup(URL_REGEX.matcher(relation.getTargetId()), "id");
		if (discogsId.isEmpty()) {
			LOGGER.warn("Could not find discogs ID: '{}'.", relation.getTargetId());
			return Set.of();
		}
		return discogsQueryService.lookUpRelease(discogsId.get())
			.map(release -> genreMatcherService.match(extractGenres(release)))
			.orElse(Set.of());
	}


	private Set<String> extractGenres(DiscogsRelease release) {
		Set<String> genres = new HashSet<>(release.genres());
		if (release.styles() != null) {
			genres.addAll(release.styles());
		}
		return genres;
	}

	@Override
	public boolean isRelationSupported(RelationWs2 relation) {
		return "http://musicbrainz.org/ns/rel-2.0#discogs".equals(relation.getType()) &&
			   "http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType());
	}

	@Override

	public DataType getDataType() {
		return DataType.RELEASE;
	}
}
