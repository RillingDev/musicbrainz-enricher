package org.felixrilling.musicbrainzenricher.api.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

// https://github.com/thelinmichael/spotify-web-api-java
// https://developer.spotify.com/documentation/web-api/guides/
@Service
public class SpotifyQueryService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyQueryService.class);

    private final SpotifyBucketProvider spotifyBucketProvider;
    private final BucketService bucketService;

    @Value("${musicbrainz-enricher.spotify.client-id}")
    private String clientId;

    @Value("${musicbrainz-enricher.spotify.client-secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;
    private Instant tokenExpiration;

    SpotifyQueryService(SpotifyBucketProvider spotifyBucketProvider, BucketService bucketService) {
        this.spotifyBucketProvider = spotifyBucketProvider;
        this.bucketService = bucketService;
    }

    public @NotNull Optional<Album> lookUpRelease(@NotNull final String id) {
        if (hasMissingCredentials()) {
            logger.warn("No credentials set, skipping lookup.");
            return Optional.empty();
        }

        bucketService.consumeSingleBlocking(spotifyBucketProvider.getBucket());

        try {
            return Optional.of(getApi().getAlbum(id).build().execute());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.warn("Could not look up album.", e);
            return Optional.empty();
        }
    }

    private boolean hasMissingCredentials() {
        return StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret);
    }

    private @NotNull SpotifyApi getApi() throws IOException, SpotifyWebApiException, ParseException {
        if (spotifyApi == null) {
            if (hasMissingCredentials()) {
                throw new IllegalStateException("No client id or secret provided.");
            }
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .build();
        }

        Instant now = Instant.now();
        if (tokenExpiration == null || tokenExpiration.isBefore(now)) {
            ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            tokenExpiration = now.plusSeconds(credentials.getExpiresIn());
        }

        return spotifyApi;
    }
}
