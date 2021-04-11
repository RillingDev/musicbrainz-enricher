package org.felixrilling.musicbrainzenricher.api.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

// https://github.com/thelinmichael/spotify-web-api-java
// https://developer.spotify.com/documentation/web-api/guides/
@Service
public class SpotifyQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyQueryService.class);

    private final SpotifyBucketProvider spotifyBucketProvider;
    private final BucketService bucketService;

    @Value("${musicbrainz-enricher.spotify.client-id}")
    private String clientId;

    @Value("${musicbrainz-enricher.spotify.client-secret}")
    private String clientSecret;

    // De-facto final. May be null if no credentials exist.
    // Note that getAuthorizedApiClient() should be used for API calls.
    private SpotifyApi apiClient;

    private Instant tokenExpiration;

    SpotifyQueryService(SpotifyBucketProvider spotifyBucketProvider, BucketService bucketService) {
        this.spotifyBucketProvider = spotifyBucketProvider;
        this.bucketService = bucketService;
    }

    @PostConstruct
    void init() {
        apiClient = createApiClient();
    }

    public @NotNull Optional<Album> lookUpRelease(@NotNull final String id) {
        if (apiClient == null) {
            LOGGER.warn("No credentials set, skipping lookup.");
            return Optional.empty();
        }

        bucketService.consumeSingleBlocking(spotifyBucketProvider.getBucket());

        try {
            GetAlbumRequest request = getAuthorizedApiClient().getAlbum(id).build();
            return Optional.of(request.execute());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.warn("Could not look up album.", e);
            return Optional.empty();
        }
    }

    // https://github.com/thelinmichael/spotify-web-api-java#client-credentials-flow
    private @NotNull SpotifyApi getAuthorizedApiClient() throws IOException, SpotifyWebApiException, ParseException {
        if (apiClient == null) {
            throw new IllegalStateException("Cannot authorize client if none is set.");
        }

        Instant now = Instant.now();
        if (tokenExpiration == null || tokenExpiration.isBefore(now)) {
            ClientCredentials credentials = apiClient.clientCredentials().build().execute();
            apiClient.setAccessToken(credentials.getAccessToken());
            tokenExpiration = now.plusSeconds(credentials.getExpiresIn());
        }
        return apiClient;
    }

    private @Nullable SpotifyApi createApiClient() {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            LOGGER.warn("No credentials set, skipping API client creation.");
            return null;
        }
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }

}
