package dev.rilling.musicbrainzenricher.api.discogs;

import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

// https://www.discogs.com/developers/
@Service
@ThreadSafe
public class DiscogsQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscogsQueryService.class);

	private final Bucket bucket;
	private final RestClient restClient;

	DiscogsQueryService(Environment environment,
						@Qualifier("discogsBucket") Bucket bucket) {
		this.bucket = bucket;

		restClient = createRestClient(environment);
	}


	public Optional<DiscogsRelease> lookUpRelease(final String id) {
		bucket.asBlocking().consumeUninterruptibly(1);

		try {
			return Optional.ofNullable(restClient.get().uri("/releases/{id}", Map.of("id", id)).accept().retrieve().body(DiscogsRelease.class));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up release '{}'.", id, e);
			return Optional.empty();
		}
	}


	public Optional<DiscogsMaster> lookUpMaster(final String id) {
		bucket.asBlocking().consumeUninterruptibly(1);

		try {
			return Optional.ofNullable(restClient.get().uri("/masters/{id}", Map.of("id", id)).retrieve().body(DiscogsMaster.class));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up master '{}'.", id, e);
			return Optional.empty();
		}
	}


	private static RestClient createRestClient(Environment environment) {
		String applicationName = environment.getRequiredProperty("musicbrainz-enricher.name");
		String applicationVersion = environment.getRequiredProperty("musicbrainz-enricher.version");
		String applicationContact = environment.getRequiredProperty("musicbrainz-enricher.contact");
		// See https://www.discogs.com/developers/
		String userAgent = "%s/%s +%s".formatted(applicationName, applicationVersion, applicationContact);

		RestClient.Builder builder = RestClient.builder()
			.baseUrl("https://api.discogs.com")
			.defaultHeader(HttpHeaders.USER_AGENT, userAgent)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

		if (environment.containsProperty("musicbrainz-enricher.discogs.token")) {
			// https://www.discogs.com/developers/#page:authentication
			builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Discogs token=%s".formatted(environment.getProperty("musicbrainz-enricher.discogs.token")));
		}

		return builder.build();
	}

}
