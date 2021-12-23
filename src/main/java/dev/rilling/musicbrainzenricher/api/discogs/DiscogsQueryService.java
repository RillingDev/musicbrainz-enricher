package dev.rilling.musicbrainzenricher.api.discogs;

import dev.rilling.musicbrainzenricher.api.BucketService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

// https://www.discogs.com/developers/
@Service
@ThreadSafe
public class DiscogsQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscogsQueryService.class);

	private final DiscogsBucketProvider discogsBucketProvider;
	private final BucketService bucketService;
	private final RestTemplateBuilder restTemplateBuilder;

	private final RestTemplate webClient;

	DiscogsQueryService(Environment environment,
						DiscogsBucketProvider discogsBucketProvider,
						BucketService bucketService,
						RestTemplateBuilder restTemplateBuilder) {
		this.discogsBucketProvider = discogsBucketProvider;
		this.bucketService = bucketService;
		this.restTemplateBuilder = restTemplateBuilder;

		String applicationName = environment.getRequiredProperty("musicbrainz-enricher.name");
		String applicationVersion = environment.getRequiredProperty("musicbrainz-enricher.version");
		String applicationContact = environment.getRequiredProperty("musicbrainz-enricher.contact");
		String token = environment.getProperty("musicbrainz-enricher.discogs.token");
		webClient = createWebClient(applicationName, applicationVersion, applicationContact, token);
	}

	@NotNull
	public Optional<DiscogsRelease> lookUpRelease(@NotNull final String id) {
		bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

		try {
			return Optional.ofNullable(webClient.getForObject("/releases/{id}",
				DiscogsRelease.class,
				Map.of("id", id)));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up release '{}'.", id, e);
			return Optional.empty();
		}
	}

	@NotNull
	public Optional<DiscogsMaster> lookUpMaster(@NotNull final String id) {
		bucketService.consumeSingleBlocking(discogsBucketProvider.getBucket());

		try {
			return Optional.ofNullable(webClient.getForObject("/masters/{id}", DiscogsMaster.class, Map.of("id", id)));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up master '{}'.", id, e);
			return Optional.empty();
		}
	}

	@NotNull
	private RestTemplate createWebClient(@NotNull String applicationName,
										 @NotNull String applicationVersion,
										 @NotNull String applicationContact,
										 String token) {
		String userAgent = getUserAgent(applicationName, applicationVersion, applicationContact);

		RestTemplateBuilder builder = restTemplateBuilder.rootUri("https://api.discogs.com")
			.defaultHeader(HttpHeaders.USER_AGENT, userAgent)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		if (!StringUtils.isEmpty(token)) {
			// https://www.discogs.com/developers/#page:authentication
			builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Discogs token=%s".formatted(token));
		}
		return builder.build();
	}

	private @NotNull String getUserAgent(@NotNull String applicationName,
										 @NotNull String applicationVersion,
										 @NotNull String applicationContact) {
		// See https://www.discogs.com/developers/
		return "%s/%s +%s".formatted(applicationName, applicationVersion, applicationContact);
	}

}
