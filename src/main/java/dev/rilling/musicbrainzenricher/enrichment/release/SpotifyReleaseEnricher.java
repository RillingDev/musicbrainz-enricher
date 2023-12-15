package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.spotify.SpotifyQueryService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.core.genre.GenreMatcherService;
import dev.rilling.musicbrainzenricher.enrichment.GenreEnricher;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://musicbrainz.org/release/5bcb2971-fdea-4543-baf2-dd41d8b9a3cd
// https://open.spotify.com/album/0Q2o6ioxIOlKPvRdG1K5da
@Service
@ThreadSafe
class SpotifyReleaseEnricher implements GenreEnricher {

	private static final Pattern URL_REGEX = Pattern.compile("https?://open\\.spotify\\.com/album/(?<id>\\w+)");

	private final GenreMatcherService genreMatcherService;
	private final SpotifyQueryService spotifyQueryService;

	SpotifyReleaseEnricher(GenreMatcherService genreMatcherService, SpotifyQueryService spotifyQueryService) {
		this.genreMatcherService = genreMatcherService;
		this.spotifyQueryService = spotifyQueryService;
	}

	@Override

	public Set<String> fetchGenres( RelationWs2 relation) {
		return spotifyQueryService.lookUpRelease(findReleaseId(relation.getTargetId())).map(release -> {
			Set<String> genres = new HashSet<>(Arrays.asList(release.getGenres()));
			return genreMatcherService.match(genres);
		}).orElse(Set.of());
	}


	private String findReleaseId( String relationUrl) {
		Matcher matcher = URL_REGEX.matcher(relationUrl);
		//noinspection ResultOfMethodCallIgnored We know we matched in #relationFits
		matcher.matches();
		return matcher.group("id");
	}

	@Override
	public boolean isRelationSupported( RelationWs2 relation) {
		if (!"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType())) {
			return false;
		}

		String targetUrl = relation.getTargetId();
		return URL_REGEX.matcher(targetUrl).matches();
	}

	@Override

	public DataType getDataType() {
		return DataType.RELEASE;
	}
}
