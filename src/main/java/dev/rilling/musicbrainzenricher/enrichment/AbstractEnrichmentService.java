package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.core.DataTypeAware;
import dev.rilling.musicbrainzenricher.util.MergeUtils;
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
	private static final double MIN_GENRE_USAGE = 0.90;

	private final ApplicationContext applicationContext;
	private final CompletionService<EnricherResult> completionService;
	private final MusicbrainzEditController musicbrainzEditController;

	protected AbstractEnrichmentService(ApplicationContext applicationContext, ExecutorService executorService, MusicbrainzEditController musicbrainzEditController) {
		this.applicationContext = applicationContext;
		completionService = new ExecutorCompletionService<>(executorService);
		this.musicbrainzEditController = musicbrainzEditController;
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
					futures.add(completionService.submit(() -> {
						LOGGER.debug("Starting enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);
						Set<String> genres = enricher.fetchGenres(relation);
						LOGGER.debug("Enricher {} found genres '{}' for '{}'.",
							enricher.getClass().getSimpleName(),
							genres,
							relation);
						return new EnricherResult(genres);
					}));
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

		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(EnricherResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);
		if (!newGenres.isEmpty()) {
			ReleaseGroupWs2 targetEntity = extractTargetEntity(entity);
			LOGGER.info("Submitting new tags '{}' for the release group '{}'.", newGenres, targetEntity.getId());
			musicbrainzEditController.submitReleaseGroupUserTags(targetEntity, newGenres);
		}
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

	public record EnricherResult(Set<String> genres) {
	}
}
