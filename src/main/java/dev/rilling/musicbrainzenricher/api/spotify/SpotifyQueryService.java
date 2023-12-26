package dev.rilling.musicbrainzenricher.api.spotify;

import io.github.bucket4j.Bucket;
import jakarta.annotation.Nullable;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

// https://github.com/thelinmichael/spotify-web-api-java
// https://developer.spotify.com/documentation/web-api/guides/
@Service
@ConditionalOnBean(SpotifyApi.class)
@ThreadSafe
public class SpotifyQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyQueryService.class);

	private final Bucket bucket;
	private final SpotifyApi spotifyApi;

	@GuardedBy("spotifyApi")
	@Nullable
	private Instant tokenExpiration;

	SpotifyQueryService(SpotifyApi spotifyApi,
						@Qualifier("spotifyBucket") Bucket bucket) {
		this.spotifyApi = spotifyApi;
		this.bucket = bucket;
	}


	public Optional<Album> lookUpRelease(final String id) {
		bucket.asBlocking().consumeUninterruptibly(1);

		try {
			GetAlbumRequest request = getAuthorizedApiClient().getAlbum(id).build();
			return Optional.of(request.execute());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			LOGGER.warn("Could not look up album.", e);
			return Optional.empty();
		}
	}

	// https://github.com/thelinmichael/spotify-web-api-java#client-credentials-flow
	private SpotifyApi getAuthorizedApiClient() throws IOException, SpotifyWebApiException, ParseException {
		Instant now = Instant.now();
		synchronized (spotifyApi) {
			if (tokenExpiration == null || tokenExpiration.isBefore(now)) {
				ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
				spotifyApi.setAccessToken(credentials.getAccessToken());
				tokenExpiration = now.plusSeconds(credentials.getExpiresIn());
			}
		}
		return spotifyApi;
	}

}
