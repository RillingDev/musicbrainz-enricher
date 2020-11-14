package org.felixrilling.musicbrainzenricher.io;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

@Service
public class BucketService {

    public void consumeSingleBlocking(Bucket bucket) {
        try {
            bucket.asScheduler().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
