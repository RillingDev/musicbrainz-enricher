package org.felixrilling.musicbrainzenricher.io.discogs;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

// https://www.discogs.com/developers/
@Service
public class DiscogsQueryService {

    @Value("${musicbrainz.enricher.client.rfc1945}")
    private String client;

    public @NotNull Optional<DiscogsRelease> lookUpRelease(@NotNull final String id) {
        HttpResponse<DiscogsRelease> response = Unirest.get("https://api.discogs.com/releases/{id}").routeParam("id", id)
                .header("User-Agent", client)
                .asObject(DiscogsRelease.class);
        return response.isSuccess() ? Optional.of(response.getBody()) : Optional.empty();
    }
}
