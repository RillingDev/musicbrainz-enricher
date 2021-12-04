package org.felixrilling.musicbrainzenricher.api;

import io.github.bucket4j.Bucket;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public interface BucketProvider {

	@NotNull Bucket getBucket();

}
