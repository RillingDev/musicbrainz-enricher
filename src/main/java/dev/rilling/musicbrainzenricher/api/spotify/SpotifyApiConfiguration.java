package dev.rilling.musicbrainzenricher.api.spotify;

import dev.rilling.musicbrainzenricher.api.LoggingBucketListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ThreadSafe
class SpotifyApiConfiguration {

	@Bean("spotifyBucket")
	Bucket spotifyBucket() {
		// https://developer.spotify.com/documentation/web-api/guides/rate-limits/
		// Spotify itself does not disclose an exact rate, this is only a guess to avoid running into it.
		Bandwidth bandwidth = Bandwidth.simple(10, Duration.ofMinutes(1));

		return Bucket.builder().addLimit(bandwidth).build().toListenable(new LoggingBucketListener("spotify"));
	}
}
