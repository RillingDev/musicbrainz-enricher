package org.felixrilling.musicbrainzenricher.enrichment;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEnrichmentService<TEntity, UResult> implements DataTypeAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEnrichmentService.class);

	private final ApplicationContext applicationContext;

	protected AbstractEnrichmentService(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void executeEnrichment(@NotNull UUID mbid) {
		Optional<TEntity> entityOptional = fetchEntity(mbid);
		if (entityOptional.isEmpty()) {
			LOGGER.warn("Could not find '{}' for data-type '{}'.", mbid, getDataType());
			return;
		}
		TEntity entity = entityOptional.get();

		Set<Enricher> enrichers = findFittingEnrichers();
		Set<UResult> results = new HashSet<>(enrichers.size());
		for (RelationWs2 relation : extractRelations(entity)) {
			for (Enricher enricher : enrichers) {
				if (enricher.isRelationSupported(relation)) {
					results.add(enrich(entity, relation, enricher));
				}
			}
			if (results.isEmpty()) {
				LOGGER.debug("Could not find any enricher for '{}'.", relation.getTargetId());
			}
		}

		updateEntity(entity, mergeResults(results));
	}

	protected abstract @NotNull Optional<TEntity> fetchEntity(@NotNull UUID mbid);

	protected abstract @NotNull Collection<RelationWs2> extractRelations(@NotNull TEntity entity);

	protected abstract @NotNull UResult enrich(@NotNull TEntity entity,
											   @NotNull RelationWs2 relation,
											   @NotNull Enricher enricher);

	protected abstract @NotNull UResult mergeResults(@NotNull Collection<UResult> results);

	protected abstract void updateEntity(@NotNull TEntity entity, @NotNull UResult result);

	private @NotNull Set<Enricher> findFittingEnrichers() {
		return applicationContext.getBeansOfType(Enricher.class)
			.values()
			.stream()
			.filter(enricher -> enricher.getDataType() == getDataType())
			.collect(Collectors.toUnmodifiableSet());
	}
}
