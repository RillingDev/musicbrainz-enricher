package org.felixrilling.musicbrainzenricher.api.discogs;

import org.apache.commons.lang3.StringUtils;
import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

// https://www.discogs.com/developers/
@Service
public class DiscogsQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscogsQueryService.class);
    private static final String BASE_URL = "https://api.discogs.com";

    private final DiscogsBucketProvider discogsBucketProvider;
    private final BucketService bucketService;
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${musicbrainz-enricher.name}")
    private String applicationName;

    @Value("${musicbrainz-enricher.version}")
    private String applicationVersion;

    @Value("${musicbrainz-enricher.contact}")
    private String applicationContact;

    @Value("${musicbrainz-enricher.discogs.token}")
    private String token;

    public DiscogsQueryService(DiscogsBucketProvider discogsBucketProvider, BucketService bucketService, RestTemplateBuilder restTemplateBuilder) {
        this.discogsBucketProvider = discogsBucketProvider;
        this.bucketService = bucketService;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public @NotNull Optional<DiscogsRelease> lookUpRelease(@NotNull final String id) {
        bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

        try {
            return Optional.ofNullable(createWebClient()
                    .getForObject(BASE_URL + "/releases/{id}", DiscogsRelease.class, Map.of("id", id)));
        } catch (RestClientException e) {
            LOGGER.warn("Could not look up release '{}'.", id, e);
            return Optional.empty();
        }
    }

    public @NotNull Optional<DiscogsMaster> lookUpMaster(@NotNull final String id) {
        bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

        try {
            return Optional.ofNullable(createWebClient()
                    .getForObject(BASE_URL + "/masters/{id}", DiscogsMaster.class, Map.of("id", id)));
        } catch (RestClientException e) {
            LOGGER.warn("Could not look up master '{}'.", id, e);
            return Optional.empty();
        }
    }

    private @NotNull RestTemplate createWebClient() {
        // See https://www.discogs.com/developers/
        String userAgent = String.format("%s/%s +%s", applicationName, applicationVersion, applicationContact);

        RestTemplateBuilder builder = restTemplateBuilder.defaultHeader(HttpHeaders.USER_AGENT, userAgent);
        if (!StringUtils.isEmpty(token)) {
            // https://www.discogs.com/developers/#page:authentication
            builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Discogs token=%s", token));
        }
        return builder.build();
    }

}
