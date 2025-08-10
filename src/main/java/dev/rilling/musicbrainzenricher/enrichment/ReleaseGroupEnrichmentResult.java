package dev.rilling.musicbrainzenricher.enrichment;

import java.util.UUID;

public record ReleaseGroupEnrichmentResult(UUID gid, String genre) {
	// TODO include enricher type or URL for debugging
}
