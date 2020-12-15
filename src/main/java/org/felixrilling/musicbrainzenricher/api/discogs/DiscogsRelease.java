package org.felixrilling.musicbrainzenricher.api.discogs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * (Incomplete) Discogs release.
 */
// See e.g. https://api.discogs.com/releases/249504
public class DiscogsRelease {
    private final Set<String> genres;
    private final Set<String> styles;

    @JsonCreator
    public DiscogsRelease(
            @JsonProperty(value = "genres", required = true) Set<String> genres,
            @JsonProperty(value = "styles", required = true) Set<String> styles
    ) {
        this.genres = genres;
        this.styles = styles;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public Set<String> getStyles() {
        return styles;
    }
}
