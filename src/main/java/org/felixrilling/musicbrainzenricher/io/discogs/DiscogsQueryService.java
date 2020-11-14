package org.felixrilling.musicbrainzenricher.io.discogs;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.felixrilling.musicbrainzenricher.io.BucketService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

// https://www.discogs.com/developers/
@Service
public class DiscogsQueryService {

    @Value("${musicbrainz.enricher.client.rfc1945}")
    private String client;

    private final DiscogsBucketProvider discogsBucketProvider;
    private final BucketService bucketService;

    public DiscogsQueryService(DiscogsBucketProvider discogsBucketProvider, BucketService bucketService) {
        this.discogsBucketProvider = discogsBucketProvider;
        this.bucketService = bucketService;
    }

    public @NotNull Optional<DiscogsRelease> lookUpRelease(@NotNull final String id) {
        bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

        return wrapResponse(Unirest.get("https://api.discogs.com/releases/{id}").routeParam("id", id)
                .header("User-Agent", client)
                .asObject(DiscogsRelease.class));
    }


    private <T> @NotNull Optional<T> wrapResponse(@NotNull HttpResponse<T> response) {
        return response.isSuccess() ? Optional.of(response.getBody()) : Optional.empty();
    }
}
