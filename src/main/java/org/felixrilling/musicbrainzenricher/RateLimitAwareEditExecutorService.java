package org.felixrilling.musicbrainzenricher;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RateLimitAwareEditExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAwareEditExecutorService.class);

    // Based on per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting
    private final static Bandwidth limit = Bandwidth.simple(1, Duration.ofSeconds(1));

    private final LocalBucket bucket = Bucket4j.builder().addLimit(limit).build();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void submit(Runnable editRunnable) {
        executorService.submit(() -> {
            logger.debug("Attempting to consume token...");
            try {
                // Block until token is available.
                bucket.asScheduler().consume(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.debug("Starting edit...");
            try {
                editRunnable.run();
                logger.debug("Completed edit.");
            } catch (Exception e) {
                logger.error("Runnable threw exception.", e);
            }
        });
    }
}
