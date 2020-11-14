package org.felixrilling.musicbrainzenricher.io;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BucketService {

    private static final Logger logger = LoggerFactory.getLogger(BucketService.class);

    public void consumeSingleBlocking(Bucket bucket) {
        try {
            logger.trace("Attempting to consume token from bucket '{}'.", bucket);
            bucket.asScheduler().consume(1);
            logger.trace("Consumed token from bucket '{}'.", bucket);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
