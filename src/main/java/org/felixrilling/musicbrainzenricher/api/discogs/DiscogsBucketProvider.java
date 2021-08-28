package org.felixrilling.musicbrainzenricher.api.discogs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.SynchronizationStrategy;
import org.apache.commons.lang3.StringUtils;
import org.felixrilling.musicbrainzenricher.api.BucketProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Scope("singleton")
class DiscogsBucketProvider implements BucketProvider {

    @Value("${musicbrainz-enricher.discogs.token}")
    private String token;

    //https://www.discogs.com/developers/#page:home,header:home-rate-limiting
    // Capacity increased if authenticated.
    private final Bandwidth bandwidth = Bandwidth.simple(StringUtils.isEmpty(token) ? 25 : 60, Duration.ofMinutes(1));

    private final Bucket bucket = Bucket4j.builder().addLimit(bandwidth).withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE).build();

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
