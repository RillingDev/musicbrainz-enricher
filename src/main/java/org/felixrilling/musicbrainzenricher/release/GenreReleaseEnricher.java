package org.felixrilling.musicbrainzenricher.release;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface GenreReleaseEnricher extends ReleaseEnricher {
    @NotNull Set<String> fetchGenres(@NotNull String relationUrl) throws Exception;
}
