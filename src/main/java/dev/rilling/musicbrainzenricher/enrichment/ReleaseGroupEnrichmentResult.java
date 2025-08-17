package dev.rilling.musicbrainzenricher.enrichment;

import java.util.UUID;

public record ReleaseGroupEnrichmentResult(UUID targetMbid, String genre) {
}
