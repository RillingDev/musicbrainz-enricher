package dev.rilling.musicbrainzenricher.api.spotify;

import dev.rilling.musicbrainzenricher.api.LoggingBucketListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.michaelthelin.spotify.SpotifyApi;

import java.time.Duration;

@Configuration
@ConditionalOnProperty({"musicbrainz-enricher.spotify.client-id", "musicbrainz-enricher.spotify.client-secret"})
@ThreadSafe
class SpotifyApiConfiguration {

	@Bean("spotifyBucket")
	Bucket spotifyBucket() {
		// https://developer.spotify.com/documentation/web-api/guides/rate-limits/
		// Spotify itself does not disclose an exact rate, this is only a guess to avoid running into it.
		Bandwidth bandwidth = Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build();

		return Bucket.builder().addLimit(bandwidth).build().toListenable(new LoggingBucketListener("spotify"));
	}

	@Bean
	SpotifyApi createApiClient(Environment environment) {
		return new SpotifyApi.Builder()
			.setClientId(environment.getRequiredProperty("musicbrainz-enricher.spotify.client-id"))
			.setClientSecret(environment.getRequiredProperty("musicbrainz-enricher.spotify.client-secret"))
			.build();
	}

}
