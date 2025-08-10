package dev.rilling.musicbrainzenricher.api.discogs;

import dev.rilling.musicbrainzenricher.api.LoggingBucketListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@ThreadSafe
class DiscogsApiConfiguration {

	@Bean("discogsBucket")
	Bucket discogsBucket(Environment environment) {
		boolean authenticated = environment.containsProperty("musicbrainz-enricher.discogs.token");

		// See https://www.discogs.com/developers/#page:home,header:home-rate-limiting,
		// further slowed down to adapt for network fluctuations.
		int capacity = authenticated ? 60 : 25;
		Bandwidth bandwidth = Bandwidth.builder().capacity(capacity).refillGreedy(capacity, Duration.ofSeconds(90)).build();

		return Bucket.builder().addLimit(bandwidth).build().toListenable(new LoggingBucketListener("discogs"));
	}

	@Bean("discogsRestClient")
	RestClient discogsRestClient(Environment environment) {
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
