package org.felixrilling.musicbrainzenricher.api.discogs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * (Incomplete) Discogs master.
 */
// See e.g. https://api.discogs.com/masters/1640131
public class DiscogsMaster {
    private final @NotNull Set<String> genres;
    private final Set<String> styles;

    @JsonCreator
    public DiscogsMaster(
            @JsonProperty(value = "genres", required = true) Set<String> genres,
            @JsonProperty(value = "styles") Set<String> styles
    ) {
        this.genres = Set.copyOf(genres);
        this.styles = styles != null ? Set.copyOf(styles) : null;
    }

    public @NotNull Set<String> getGenres() {
        return genres;
    }

    public Set<String> getStyles() {
        return styles;
    }
}
