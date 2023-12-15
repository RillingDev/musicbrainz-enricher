package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Component;

/**
 * An enricher is a component takes a relation ({@link RelationWs2}) of a Musicbrainz entity
 * and calculates additional data based on it. Sub-interfaces should specify additional methods which
 * return additional, calculated data for a relation.
 */
@Component
public interface Enricher extends DataTypeAware {

	/**
	 * Checks if a relation of the data type is supported for enrichment.
	 * Often enrichers check if e.g. an URL stored in the relation is supported.
	 *
	 * @param relation Relation to check.
	 * @return if the relation is supported.
	 */
	boolean isRelationSupported( RelationWs2 relation);

}
