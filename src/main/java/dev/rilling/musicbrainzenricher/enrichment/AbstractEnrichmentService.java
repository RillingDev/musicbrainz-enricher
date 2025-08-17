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
	private final CompletionService<Set<String>> completionService;

	protected AbstractEnrichmentService(ApplicationContext applicationContext, ExecutorService executorService) {
		this.applicationContext = applicationContext;
		completionService = new ExecutorCompletionService<>(executorService);
	}

	public Optional<Set<ReleaseGroupEnrichmentResult>> executeEnrichment(UUID sourceMbid) {
		Optional<TEntity> entityOptional = fetchEntity(sourceMbid);
		if (entityOptional.isEmpty()) {
			LOGGER.warn("Could not find '{}' for the data type '{}'.", sourceMbid, getDataType());
			return Optional.empty();
		}
		TEntity entity = entityOptional.get();

		Set<Enricher> enrichers = findFittingEnrichers();
		Collection<RelationWs2> relations = extractRelations(entity);
		Set<Future<Set<String>>> futures = new HashSet<>(enrichers.size() * relations.size());
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
						return Collections.unmodifiableSet(genres);
					}));
				}
			}
		}

		Set<String> genres = new HashSet<>();
		int received = 0;
		while (received < futures.size()) {
			try {
				genres.addAll(
					// Blocks if none available
					completionService.take().get()
				);
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

		final UUID targetMbid = UUID.fromString(extractTargetEntity(entity).getId());
		Set<ReleaseGroupEnrichmentResult> results = genres.stream().map(genre -> new ReleaseGroupEnrichmentResult(targetMbid, genre)).collect(Collectors.toUnmodifiableSet());
		return Optional.of(results);
	}


	protected abstract Optional<TEntity> fetchEntity(UUID sourceMbid);


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
