package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnricherService {

	private final ApplicationContext applicationContext;

	EnricherService(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public @NotNull Set<Enricher> findFittingEnrichers(@NotNull final EnrichmentService enrichmentService) {
		return applicationContext.getBeansOfType(Enricher.class).values()
			.stream()
			.filter(enricher -> enricher.getDataType() == enrichmentService.getDataType())
			.collect(Collectors.toUnmodifiableSet());
	}
}
