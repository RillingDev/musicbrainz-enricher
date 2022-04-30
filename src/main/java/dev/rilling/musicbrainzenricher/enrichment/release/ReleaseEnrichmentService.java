package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.Enricher;
import dev.rilling.musicbrainzenricher.enrichment.GenreEnricher;
import dev.rilling.musicbrainzenricher.util.MergeUtils;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class ReleaseEnrichmentService extends AbstractEnrichmentService<ReleaseWs2, ReleaseEnrichmentService.ReleaseEnrichmentResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

	private static final double MIN_GENRE_USAGE = 0.90;

	private final MusicbrainzLookupService musicbrainzLookupService;
	private final MusicbrainzEditController musicbrainzEditController;

	ReleaseEnrichmentService(ApplicationContext applicationContext,
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
		return DataType.RELEASE;
	}

	@Override
	@NotNull
	protected Optional<ReleaseWs2> fetchEntity(@NotNull UUID mbid) {
		ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);
		includes.setReleaseGroups(true);

		try {
			return musicbrainzLookupService.lookUpRelease(mbid, includes);
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not query release '{}'.", mbid, e);
			return Optional.empty();
		}
	}

	@Override
	@NotNull
	protected Collection<RelationWs2> extractRelations(@NotNull ReleaseWs2 releaseWs2) {
		return releaseWs2.getRelationList().getRelations();
	}

	@Override
	@NotNull
	protected ReleaseEnrichmentResult enrich(@NotNull ReleaseWs2 release,
											 @NotNull RelationWs2 relation,
											 @NotNull Enricher enricher) {
		LOGGER.debug("Starting enricher '{}' for '{}'.", enricher, relation);
		Set<String> newGenres = new HashSet<>(5);
		if (enricher instanceof GenreEnricher genreEnricher) {
			Set<String> genres = genreEnricher.fetchGenres(relation);
			LOGGER.debug("Enricher '{}' found genres '{}' for release '{}'.",
				genreEnricher.getClass().getSimpleName(),
				genres,
				release.getId());

			newGenres.addAll(genres);
		}
		LOGGER.debug("Completed enricher '{}' for '{}'.", enricher, relation);
		return new ReleaseEnrichmentResult(newGenres);
	}

	@Override
	@NotNull
	protected ReleaseEnrichmentResult mergeResults(@NotNull Collection<ReleaseEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseEnrichmentResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);

		return new ReleaseEnrichmentResult(newGenres);
	}

	@Override
	protected void updateEntity(@NotNull ReleaseWs2 release, @NotNull ReleaseEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			ReleaseGroupWs2 releaseGroup = release.getReleaseGroup();
			LOGGER.info("Submitting new tags '{}' for release group '{}'.", result.genres(), releaseGroup.getId());
			musicbrainzEditController.submitReleaseGroupUserTags(releaseGroup, result.genres());
		}
	}

	protected record ReleaseEnrichmentResult(@NotNull Set<String> genres) {
	}

}
