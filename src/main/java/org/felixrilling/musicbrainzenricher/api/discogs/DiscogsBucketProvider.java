package org.felixrilling.musicbrainzenricher.api.discogs;

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
class DiscogsBucketProvider implements BucketProvider {

    //https://www.discogs.com/developers/#page:home,header:home-rate-limiting
    // 60/m Increased by 15% to account for network oddities
    private static final Bandwidth BANDWIDTH = Bandwidth.simple(60, Duration.ofSeconds(Math.round(60 * 1.15)));

    private final Bucket bucket = Bucket4j.builder().addLimit(BANDWIDTH).withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE).build();

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
