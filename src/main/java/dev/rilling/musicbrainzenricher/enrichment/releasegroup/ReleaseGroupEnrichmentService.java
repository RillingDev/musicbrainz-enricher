package dev.rilling.musicbrainzenricher.enrichment.releasegroup;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.Enricher;
import dev.rilling.musicbrainzenricher.enrichment.GenreEnricher;
import dev.rilling.musicbrainzenricher.util.MergeUtils;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class ReleaseGroupEnrichmentService extends AbstractEnrichmentService<ReleaseGroupWs2, ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupEnrichmentService.class);

	private static final double MIN_GENRE_USAGE = 0.90;

	private final MusicbrainzLookupService musicbrainzLookupService;
	private final MusicbrainzEditController musicbrainzEditController;

	ReleaseGroupEnrichmentService(ApplicationContext applicationContext,
								  @Qualifier("enrichmentExecutor") ExecutorService executorService,
								  MusicbrainzLookupService musicbrainzLookupService,
								  MusicbrainzEditController musicbrainzEditController) {
		super(applicationContext, executorService);
		this.musicbrainzLookupService = musicbrainzLookupService;
		this.musicbrainzEditController = musicbrainzEditController;
	}

	@Override
	@NotNull
	public DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}

	@Override
	@NotNull
	protected Optional<ReleaseGroupWs2> fetchEntity(@NotNull UUID mbid) {
		ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);

		try {
			return musicbrainzLookupService.lookUpReleaseGroup(mbid, includes);
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not query release-group '{}'.", mbid, e);
			return Optional.empty();
		}
	}

	@Override
	@NotNull
	protected Collection<RelationWs2> extractRelations(@NotNull ReleaseGroupWs2 entity) {
		return entity.getRelationList().getRelations();
	}

	@Override
	@NotNull
	protected ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult enrich(@NotNull ReleaseGroupWs2 entity,
																				@NotNull RelationWs2 relation,
																				@NotNull Enricher enricher) {
		LOGGER.debug("Starting enricher '{}' for '{}'.", enricher, relation);
		Set<String> newGenres = new HashSet<>(5);
		if (enricher instanceof GenreEnricher genreEnricher) {
			Set<String> genres = genreEnricher.fetchGenres(relation);
			LOGGER.debug("Enricher '{}' found genres '{}' for release group '{}'.",
				genreEnricher.getClass().getSimpleName(),
				genres,
				entity.getId());

			newGenres.addAll(genres);
		}
		LOGGER.debug("Completed enricher '{}' for '{}'.", enricher, relation);

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	@Override
	@NotNull
	protected ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult mergeResults(@NotNull Collection<ReleaseGroupEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseGroupEnrichmentResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	@Override
	protected void updateEntity(@NotNull ReleaseGroupWs2 entity, @NotNull ReleaseGroupEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			LOGGER.info("Submitting new tags '{}' for release group '{}'.", result.genres(), entity.getId());
			musicbrainzEditController.submitReleaseGroupUserTags(entity, result.genres());
		}
	}

	protected record ReleaseGroupEnrichmentResult(@NotNull Set<String> genres) {
	}

}
