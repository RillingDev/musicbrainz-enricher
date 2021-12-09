package org.felixrilling.musicbrainzenricher.api.discogs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.SynchronizationStrategy;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.felixrilling.musicbrainzenricher.api.BucketProvider;
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
		//https://www.discogs.com/developers/#page:home,header:home-rate-limiting
		// Capacity increased if authenticated.
		String token = environment.getProperty("musicbrainz-enricher.discogs.token");
		int capacity = StringUtils.isEmpty(token) ? 25 : 60;
		Bandwidth bandwidth = Bandwidth.simple(capacity, Duration.ofMinutes(1));

		bucket = Bucket4j.builder()
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
