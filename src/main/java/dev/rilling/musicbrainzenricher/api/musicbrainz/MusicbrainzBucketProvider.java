package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.api.BucketProvider;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import net.jcip.annotations.ThreadSafe;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope("singleton")
@ThreadSafe
class MusicbrainzBucketProvider implements BucketProvider {

	// See per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting,
	// further slowed down to adapt for network fluctuations.
	private static final Bandwidth BANDWIDTH = Bandwidth.simple(1, Duration.ofMillis(2500));

	private final Bucket bucket = Bucket.builder()
		.addLimit(BANDWIDTH)
		.withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
		.build();

	@Override

	public Bucket getBucket() {
		return bucket;
	}
}
