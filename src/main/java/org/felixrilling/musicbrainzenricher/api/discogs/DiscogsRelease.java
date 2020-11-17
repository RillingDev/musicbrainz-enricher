package org.felixrilling.musicbrainzenricher.api.discogs;


import java.util.Set;

/**
 * (Incomplete) Discogs release.
 */
// See e.g. https://api.discogs.com/releases/249504
public class DiscogsRelease extends DiscogsBaseModel {
    private Set<String> genres;
    private Set<String> styles;

    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public Set<String> getStyles() {
        return styles;
    }

    public void setStyles(Set<String> styles) {
        this.styles = styles;
    }
}
