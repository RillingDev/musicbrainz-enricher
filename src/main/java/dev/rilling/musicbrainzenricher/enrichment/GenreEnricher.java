package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.genre.GenreMatcherService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Enricher with the capability of calculating genre data.
 *
 * @see GenreMatcherService
 */
@Component
public interface GenreEnricher extends Enricher {

	/**
	 * Returns a set of musicbrainz compatible genre names that belong to the relation target.
	 *
	 * @param relation Relation.
	 * @return a set of genres.
	 */
	@NotNull Set<String> fetchGenres(@NotNull RelationWs2 relation);
}
