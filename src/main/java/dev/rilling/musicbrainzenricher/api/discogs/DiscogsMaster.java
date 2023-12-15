package dev.rilling.musicbrainzenricher.api.discogs;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * (Incomplete) Discogs master.
 */
// See e.g. https://api.discogs.com/masters/1640131
public record DiscogsMaster( Set<String> genres, Set<String> styles) {
	@JsonCreator
	public DiscogsMaster(@JsonProperty(value = "genres", required = true) Set<String> genres,
						 @JsonProperty(value = "styles") Set<String> styles) {
		this.genres = Set.copyOf(genres);
		this.styles = styles != null ? Set.copyOf(styles) : null;
	}
}
