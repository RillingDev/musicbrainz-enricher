package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * An enricher is a component takes a relation ({@link RelationWs2}) of a Musicbrainz entity
 * and calculates additional data based on it.
 */
@Component
public interface Enricher extends DataTypeAware {

	/**
	 * Checks if a relation of the data type is supported for enrichment.
	 * Often enrichers check if e.g., an URL stored in the relation is supported.
	 *
	 * @param relation Relation to check.
	 * @return if the relation is supported.
	 */
	boolean isRelationSupported(RelationWs2 relation);

	/**
	 * Returns a set of MusicBrainz compatible genre names that belong to the relation target.
	 *
	 * @param relation Relation.
	 * @return a set of genres.
	 */
	Set<String> fetchGenres(RelationWs2 relation);

}
