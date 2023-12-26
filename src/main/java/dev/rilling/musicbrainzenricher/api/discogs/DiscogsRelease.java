package dev.rilling.musicbrainzenricher.api.discogs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.Set;

/**
 * (Incomplete) Discogs release.
 */
// See e.g. https://api.discogs.com/releases/249504
public record DiscogsRelease(Set<String> genres, @Nullable Set<String> styles) {
	@JsonCreator
	public DiscogsRelease(@JsonProperty(value = "genres", required = true) Set<String> genres,
						  @JsonProperty(value = "styles") @Nullable Set<String> styles) {
		this.genres = Set.copyOf(genres);
		this.styles = styles != null ? Set.copyOf(styles) : null;
	}
}
