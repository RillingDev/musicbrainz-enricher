package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class AbstractEnrichmentService<TEntity> implements DataTypeAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEnrichmentService.class);

	private final ApplicationContext applicationContext;
	private final CompletionService<EnricherResult> completionService;

	protected AbstractEnrichmentService(ApplicationContext applicationContext, ExecutorService executorService) {
		this.applicationContext = applicationContext;
		completionService = new ExecutorCompletionService<>(executorService);
	}

	// TODO check if async handling can be simplified
	public void executeEnrichment(UUID mbid) {
		Optional<TEntity> entityOptional = fetchEntity(mbid);
		if (entityOptional.isEmpty()) {
			LOGGER.warn("Could not find '{}' for the data type '{}'.", mbid, getDataType());
			return;
		}
		TEntity entity = entityOptional.get();

		Set<Enricher> enrichers = findFittingEnrichers();
		Collection<RelationWs2> relations = extractRelations(entity);
		Set<Future<EnricherResult>> futures = new HashSet<>(enrichers.size() * relations.size());
		for (RelationWs2 relation : relations) {
			for (Enricher enricher : enrichers) {
				if (enricher.isRelationSupported(relation)) {
					futures.add(completionService.submit(() -> enrich(entity, relation, enricher)));
				}
			}
		}

		Set<EnricherResult> results = new HashSet<>(futures.size());
		int received = 0;
		while (received < futures.size()) {
			try {
				EnricherResult result = completionService.take().get(); // Blocks if none available
				results.add(result);
				received++;
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted, skipping enrichment.", e);
				Thread.currentThread().interrupt();
				return;
			} catch (ExecutionException e) {
				LOGGER.error("Execution of enricher failed.", e);
				received++;
			}
		}

		updateEntity(entity, results);
	}


	protected abstract Optional<TEntity> fetchEntity(UUID mbid);


	protected abstract Collection<RelationWs2> extractRelations(TEntity entity);


	protected abstract EnricherResult enrich(TEntity entity,
											 RelationWs2 relation,
											 Enricher enricher);


	protected abstract void updateEntity(TEntity entity, Set<EnricherResult> results);


	private Set<Enricher> findFittingEnrichers() {
		return applicationContext.getBeansOfType(Enricher.class)
			.values()
			.stream()
			.filter(enricher -> enricher.getDataType() == getDataType())
			.collect(Collectors.toUnmodifiableSet());
	}

	public record EnricherResult(Set<String> genres) {
	}
}
