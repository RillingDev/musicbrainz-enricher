package dev.rilling.musicbrainzenricher.api.discogs;

import dev.rilling.musicbrainzenricher.api.BucketProvider;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope("singleton")
@ThreadSafe
class DiscogsBucketProvider implements BucketProvider {

	private final Bucket bucket;

	DiscogsBucketProvider(Environment environment) {
		boolean authenticated = !StringUtils.isEmpty(environment.getProperty("musicbrainz-enricher.discogs.token"));

		// See https://www.discogs.com/developers/#page:home,header:home-rate-limiting,
		// further slowed down to adapt for network fluctuations.
		int capacity = authenticated ? 60 : 25;
		Bandwidth bandwidth = Bandwidth.simple(capacity, Duration.ofSeconds(90));

		bucket = Bucket.builder()
			.addLimit(bandwidth)
			.withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
			.build();
	}

	@Override
	@NotNull
	public Bucket getBucket() {
		return bucket;
	}
}
