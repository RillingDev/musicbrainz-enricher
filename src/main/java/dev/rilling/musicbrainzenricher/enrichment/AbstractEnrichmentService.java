package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class AbstractEnrichmentService<TEntity> implements DataTypeAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEnrichmentService.class);

	private final ApplicationContext applicationContext;
	private final CompletionService<EntityEnrichmentResult.RelationEnrichmentResult> completionService;

	protected AbstractEnrichmentService(ApplicationContext applicationContext, ExecutorService executorService) {
		this.applicationContext = applicationContext;
		completionService = new ExecutorCompletionService<>(executorService);
	}

	// TODO check if async handling can be simplified
	public Optional<EntityEnrichmentResult> executeEnrichment(UUID mbid) {
		Optional<TEntity> entityOptional = fetchEntity(mbid);
		if (entityOptional.isEmpty()) {
			LOGGER.warn("Could not find '{}' for the data type '{}'.", mbid, getDataType());
			return Optional.empty();
		}
		TEntity entity = entityOptional.get();

		Set<Enricher> enrichers = findFittingEnrichers();
		Collection<RelationWs2> relations = extractRelations(entity);
		Set<Future<EntityEnrichmentResult.RelationEnrichmentResult>> futures = new HashSet<>(enrichers.size() * relations.size());
		for (RelationWs2 relation : relations) {
			for (Enricher enricher : enrichers) {
				if (enricher.isRelationSupported(relation)) {
					futures.add(completionService.submit(() -> {
						LOGGER.debug("Starting enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);
						Set<String> genres = enricher.fetchGenres(relation);
						LOGGER.debug("Enricher {} found genres '{}' for '{}'.",
							enricher.getClass().getSimpleName(),
							genres,
							relation);
						return new EntityEnrichmentResult.RelationEnrichmentResult(genres);
					}));
				}
			}
		}

		Set<EntityEnrichmentResult.RelationEnrichmentResult> results = new HashSet<>(futures.size());
		int received = 0;
		while (received < futures.size()) {
			try {
				EntityEnrichmentResult.RelationEnrichmentResult result = completionService.take().get(); // Blocks if none available
				results.add(result);
				received++;
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted, skipping enrichment.", e);
				Thread.currentThread().interrupt();
				return Optional.empty();
			} catch (ExecutionException e) {
				LOGGER.error("Execution of enricher failed.", e);
				received++;
			}
		}

		return Optional.of(new EntityEnrichmentResult(extractTargetEntity(entity), Collections.unmodifiableSet(results)));
	}


	protected abstract Optional<TEntity> fetchEntity(UUID mbid);


	protected abstract Collection<RelationWs2> extractRelations(TEntity entity);

	// Note: currently, only ReleaseGroupWs2 is supported as target entity, requiring enrichers to derive that if they work on some other entity
	protected abstract ReleaseGroupWs2 extractTargetEntity(TEntity entity);

	private Set<Enricher> findFittingEnrichers() {
		return applicationContext.getBeansOfType(Enricher.class)
			.values()
			.stream()
			.filter(enricher -> enricher.getDataType() == getDataType())
			.collect(Collectors.toUnmodifiableSet());
	}

}
