package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.util.MergeUtils;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
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
	private final MusicbrainzEditService musicbrainzEditService;

	ReleaseGroupEnrichmentService(ApplicationContext applicationContext,
								  @Qualifier("executor") ExecutorService executorService,
								  MusicbrainzLookupService musicbrainzLookupService,
								  MusicbrainzEditService musicbrainzEditService) {
		super(applicationContext, executorService);
		this.musicbrainzLookupService = musicbrainzLookupService;
		this.musicbrainzEditService = musicbrainzEditService;
	}

	@Override
	public @NotNull DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}

	@Override
	protected @NotNull Optional<ReleaseGroupWs2> fetchEntity(@NotNull UUID mbid) {
		ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);
		return musicbrainzLookupService.lookUpReleaseGroup(mbid, includes);
	}

	@Override
	protected @NotNull Collection<RelationWs2> extractRelations(@NotNull ReleaseGroupWs2 releaseGroupWs2) {
		return releaseGroupWs2.getRelationList().getRelations();
	}

	@Override
	protected @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult enrich(@NotNull ReleaseGroupWs2 releaseGroup,
																						 @NotNull RelationWs2 relation,
																						 @NotNull Enricher enricher) {
		LOGGER.debug("Starting enricher '{}' for '{}'.", enricher, relation);
		Set<String> newGenres = new HashSet<>(5);
		if (enricher instanceof GenreEnricher genreEnricher) {
			Set<String> genres = genreEnricher.fetchGenres(relation);
			LOGGER.debug("Enricher '{}' found genres '{}' for release group '{}'.",
				genreEnricher.getClass().getSimpleName(),
				genres,
				releaseGroup.getId());

			newGenres.addAll(genres);
		}
		LOGGER.debug("Completed enricher '{}' for '{}'.", enricher, relation);

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	@Override
	protected @NotNull ReleaseGroupEnrichmentService.ReleaseGroupEnrichmentResult mergeResults(@NotNull Collection<ReleaseGroupEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseGroupEnrichmentResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	@Override
	protected void updateEntity(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull ReleaseGroupEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			LOGGER.info("Submitting new tags '{}' for release group '{}'.", result.genres(), releaseGroup.getId());
			try {
				musicbrainzEditService.submitReleaseGroupUserTags(releaseGroup, result.genres());
			} catch (MBWS2Exception e) {
				LOGGER.error("Could not submit tags.", e);
			}
		}
	}

	protected record ReleaseGroupEnrichmentResult(@NotNull Set<String> genres) {
	}

}
