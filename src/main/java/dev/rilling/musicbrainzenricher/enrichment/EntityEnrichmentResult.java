package dev.rilling.musicbrainzenricher.enrichment;

import org.musicbrainz.model.entity.ReleaseGroupWs2;

import java.util.Set;

public record EntityEnrichmentResult(ReleaseGroupWs2 targetEntity,
									 Set<RelationEnrichmentResult> results) {
	// TODO include enricher type or URL for debugging
	public record RelationEnrichmentResult(Set<String> genres) {
	}
}
