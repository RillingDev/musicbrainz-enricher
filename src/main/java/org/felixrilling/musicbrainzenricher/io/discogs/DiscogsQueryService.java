package org.felixrilling.musicbrainzenricher.io.discogs;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.felixrilling.musicbrainzenricher.io.BucketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

// https://www.discogs.com/developers/
@Service
public class DiscogsQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DiscogsQueryService.class);
    private final DiscogsBucketProvider discogsBucketProvider;
    private final BucketService bucketService;
    @Value("${musicbrainz-enricher.name}")
    private String applicationName;
    @Value("${musicbrainz-enricher.version}")
    private String applicationVersion;
    @Value("${musicbrainz-enricher.contact}")
    private String applicationContact;

    public DiscogsQueryService(DiscogsBucketProvider discogsBucketProvider, BucketService bucketService) {
        this.discogsBucketProvider = discogsBucketProvider;
        this.bucketService = bucketService;
    }

    public @NotNull Optional<DiscogsRelease> lookUpRelease(@NotNull final String id) {
        bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

        HttpResponse<DiscogsRelease> response = Unirest.get("https://api.discogs.com/releases/{id}").routeParam("id", id)
                .header("User-Agent", getUserAgent(applicationName, applicationVersion, applicationContact))
                .asObject(DiscogsRelease.class)
                .ifFailure(res -> logger.warn("Could not look up release '{}': {}.", id, res.getStatus()));
        return wrapResponse(response);
    }


    private <T> @NotNull Optional<T> wrapResponse(@NotNull HttpResponse<T> response) {
        if (!response.isSuccess()) {
            return Optional.empty();
        }
        return Optional.of(response.getBody());
    }

    private @NotNull String getUserAgent(@NotNull String applicationName, @NotNull String applicationVersion, @NotNull String applicationContact) {
        // See https://www.discogs.com/developers/
        return String.format("%s/%s +%s", applicationName, applicationVersion, applicationContact);
    }
}
