package org.felixrilling.musicbrainzenricher.api;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

@Component
public interface BucketProvider {

    Bucket getBucket();

}
