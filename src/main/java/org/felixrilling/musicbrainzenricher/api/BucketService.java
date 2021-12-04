package org.felixrilling.musicbrainzenricher.api;

import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ThreadSafe
public class BucketService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BucketService.class);

	@Blocking
	public void consumeSingleBlocking(@NotNull Bucket bucket) {
		try {
			LOGGER.trace("Attempting to consume token from bucket '{}'.", bucket);
			bucket.asScheduler().consume(1);
			LOGGER.trace("Consumed token from bucket '{}'.", bucket);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
