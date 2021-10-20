package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service which can take a musicbrainz ID and enrich it with additional data using {@link Enricher}s.
 */
@Service
public interface EnrichmentService extends DataTypeAware {

	/**
	 * Enriches the entity this mbid matches.
	 *
	 * @param mbid Mbid of the source entity.
	 */
	void enrich(@NotNull UUID mbid);
}
