package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Scope("singleton")
public class MusicbrainzEditExecutor {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MusicbrainzEditExecutor() {
    }

    public Future<Void> submit(@NotNull Callable<Void> callable) {
        return executorService.submit(callable);
    }
}
