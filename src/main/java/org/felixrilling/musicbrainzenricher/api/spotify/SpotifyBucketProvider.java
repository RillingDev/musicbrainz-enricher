package org.felixrilling.musicbrainzenricher.api.spotify;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.SynchronizationStrategy;
import org.felixrilling.musicbrainzenricher.api.BucketProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope("singleton")
public class SpotifyBucketProvider implements BucketProvider {

    // https://developer.spotify.com/documentation/web-api/
    private static final Bandwidth BANDWIDTH = Bandwidth.simple(1, Duration.ofSeconds(5));

    private final Bucket bucket = Bucket4j.builder().addLimit(BANDWIDTH).withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE).build();

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
