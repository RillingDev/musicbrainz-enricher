package org.felixrilling.musicbrainzenricher.enrichment;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CoreEnrichmentService {

    private final ApplicationContext applicationContext;

    CoreEnrichmentService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Set<Enricher> findFittingEnrichers(final EnrichmentService enrichmentService) {
        return applicationContext
                .getBeansOfType(Enricher.class).values().stream()
                .filter(enricher -> enricher.getDataType() == enrichmentService.getDataType())
                .collect(Collectors.toSet());
    }
}
