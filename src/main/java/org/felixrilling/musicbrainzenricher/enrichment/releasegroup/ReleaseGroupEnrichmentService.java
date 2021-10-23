package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditService;
import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.enrichment.Enricher;
import org.felixrilling.musicbrainzenricher.enrichment.EnricherService;
import org.felixrilling.musicbrainzenricher.enrichment.EnrichmentService;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.util.MergeUtils;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReleaseGroupEnrichmentService implements EnrichmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupEnrichmentService.class);

	private static final double GENRE_BEST_PERCENTILE = 70.0;

	private final EnricherService enricherService;
	private final MusicbrainzLookupService musicbrainzLookupService;
	private final MusicbrainzEditService musicbrainzEditService;

	ReleaseGroupEnrichmentService(EnricherService enricherService,
								  MusicbrainzLookupService musicbrainzLookupService,
								  MusicbrainzEditService musicbrainzEditService) {
		this.enricherService = enricherService;
		this.musicbrainzLookupService = musicbrainzLookupService;
		this.musicbrainzEditService = musicbrainzEditService;
	}

	@Override
	public void enrich(@NotNull UUID mbid) {
		Set<Enricher> enrichers = enricherService.findFittingEnrichers(this);

		ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);
		Optional<ReleaseGroupWs2> releaseGroupOptional = musicbrainzLookupService.lookUpReleaseGroup(mbid, includes);

		if (releaseGroupOptional.isEmpty()) {
			LOGGER.warn("Could not find release group '{}'.", mbid);
			return;
		}

		ReleaseGroupWs2 releaseGroup = releaseGroupOptional.get();
		LOGGER.trace("Loaded release group data: '{}'.", releaseGroup);
		Set<ReleaseGroupEnrichmentResult> results = new HashSet<>(enrichers.size());
		for (RelationWs2 relation : releaseGroup.getRelationList().getRelations()) {
			for (Enricher enricher : enrichers) {
				if (enricher.isRelationSupported(relation)) {
					results.add(executeEnrichment(releaseGroup, relation, enricher));
				}
			}
			if (results.isEmpty()) {
				LOGGER.debug("Could not find any enricher for '{}'.", relation.getTargetId());
			}
		}

		updateEntity(releaseGroup, mergeResults(results));
	}

	private @NotNull ReleaseGroupEnrichmentResult executeEnrichment(@NotNull ReleaseGroupWs2 releaseGroup,
																	@NotNull RelationWs2 relation,
																	@NotNull Enricher enricher) {
		Set<String> newGenres = new HashSet<>(5);
		if (enricher instanceof GenreEnricher genreEnricher) {
			Set<String> genres = genreEnricher.fetchGenres(relation);
			LOGGER.debug("Enricher '{}' found genres '{}' for release group '{}'.",
				genreEnricher.getClass().getSimpleName(),
				genres,
				releaseGroup.getId());

			newGenres.addAll(genres);
		}

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	private @NotNull ReleaseGroupEnrichmentResult mergeResults(@NotNull Collection<ReleaseGroupEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseGroupEnrichmentResult::genres)
			.collect(Collectors.toSet()), GENRE_BEST_PERCENTILE);

		return new ReleaseGroupEnrichmentResult(newGenres);
	}

	private void updateEntity(@NotNull ReleaseGroupWs2 releaseGroup, @NotNull ReleaseGroupEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			LOGGER.info("Submitting new tags '{}' for release group '{}'.", result.genres(), releaseGroup.getId());
			try {
				musicbrainzEditService.addReleaseGroupUserTags(UUID.fromString(releaseGroup.getId()), result.genres());
			} catch (MBWS2Exception e) {
				LOGGER.error("Could not submit tags.", e);
			}
		}
	}

	@Override
	public @NotNull DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}

	private static record ReleaseGroupEnrichmentResult(Set<String> genres) {
	}

}
