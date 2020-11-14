package org.felixrilling.musicbrainzenricher.io;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RateLimitAwareExecutorCompletionService<T> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAwareExecutorCompletionService.class);

    private final LocalBucket bucket;
    private final ExecutorCompletionService<T> executorCompletionService;

    public RateLimitAwareExecutorCompletionService(ExecutorService executorService, Bandwidth bandwidth) {
        this.executorCompletionService = new ExecutorCompletionService<>(executorService);
        this.bucket = Bucket4j.builder().addLimit(bandwidth).withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE).build();
    }

    public Future<T> submit(@NotNull Callable<T> callable) {
        return executorCompletionService.submit(() -> {
            try {
                // Block until token is available.
                bucket.asScheduler().consume(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                return callable.call();
            } catch (Exception e) {
                logger.error("Callable threw exception.", e);
                return null;
            }
        });
    }
}
