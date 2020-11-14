package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.SynchronizationStrategy;
import org.felixrilling.musicbrainzenricher.io.BucketProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope("singleton")
public class MusicbrainzBucketProvider implements BucketProvider {

    // See per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting
    // Reduced quite a bit (from 1/s) to not cause issues.
    private static final Bandwidth BANDWIDTH = Bandwidth.simple(1, Duration.ofSeconds(2));

    private final Bucket bucket = Bucket4j.builder().addLimit(BANDWIDTH).withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE).build();

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
