package dev.rilling.musicbrainzenricher.api.discogs;

import dev.rilling.musicbrainzenricher.api.LoggingBucketListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
		Bandwidth bandwidth = Bandwidth.simple(capacity, Duration.ofSeconds(90));

		return Bucket.builder().addLimit(bandwidth).build().toListenable(new LoggingBucketListener("discogs"));
	}
}
