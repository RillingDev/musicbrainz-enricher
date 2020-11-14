package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import io.github.bucket4j.Bandwidth;
import org.felixrilling.musicbrainzenricher.io.RateLimitAwareExecutorCompletionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class MusicBrainzExecutor {

    private final RateLimitAwareExecutorCompletionService<Void> rateLimitAwareExecutorCompletionService;

    public MusicBrainzExecutor() {
        // Based on per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting
        Bandwidth bandwidth = Bandwidth.simple(1, Duration.ofSeconds(1));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        this.rateLimitAwareExecutorCompletionService = new RateLimitAwareExecutorCompletionService<>(executorService, bandwidth);
    }

    public Future<Void> submit(@NotNull Callable<Void> callable) {
        return rateLimitAwareExecutorCompletionService.submit(callable);
    }
}
