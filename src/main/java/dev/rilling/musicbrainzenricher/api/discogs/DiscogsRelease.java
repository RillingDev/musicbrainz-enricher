package dev.rilling.musicbrainzenricher.api.discogs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * (Incomplete) Discogs release.
 */
// See e.g. https://api.discogs.com/releases/249504
public record DiscogsRelease( Set<String> genres, Set<String> styles) {
	@JsonCreator
	public DiscogsRelease(@JsonProperty(value = "genres", required = true) Set<String> genres,
						  @JsonProperty(value = "styles") Set<String> styles) {
		this.genres = Set.copyOf(genres);
		this.styles = styles != null ? Set.copyOf(styles) : null;
	}
}
