package org.felixrilling.musicbrainzenricher.enrichment.release;

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
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReleaseEnrichmentService implements EnrichmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

	private static final double GENRE_BEST_PERCENTILE = 70.0;

	private final EnricherService enricherService;
	private final MusicbrainzLookupService musicbrainzLookupService;
	private final MusicbrainzEditService musicbrainzEditService;

	ReleaseEnrichmentService(EnricherService enricherService,
							 MusicbrainzLookupService musicbrainzLookupService,
							 MusicbrainzEditService musicbrainzEditService) {
		this.enricherService = enricherService;
		this.musicbrainzLookupService = musicbrainzLookupService;
		this.musicbrainzEditService = musicbrainzEditService;
	}

	@Override
	public void enrich(@NotNull UUID mbid) {
		Set<Enricher> enrichers = enricherService.findFittingEnrichers(this);

		ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);
		includes.setReleaseGroups(true);
		Optional<ReleaseWs2> releaseOptional = musicbrainzLookupService.lookUpRelease(mbid, includes);

		if (releaseOptional.isEmpty()) {
			LOGGER.warn("Could not find release '{}'.", mbid);
			return;
		}

		ReleaseWs2 release = releaseOptional.get();
		LOGGER.trace("Loaded release data: '{}'.", release);
		Set<ReleaseEnrichmentResult> results = new HashSet<>(enrichers.size());
		for (RelationWs2 relation : release.getRelationList().getRelations()) {
			for (Enricher enricher : enrichers) {
				if (enricher.isRelationSupported(relation)) {
					results.add(executeEnrichment(release, relation, enricher));
				}
			}
			if (results.isEmpty()) {
				LOGGER.debug("Could not find any enricher for '{}'.", relation.getTargetId());
			}
		}

		updateEntity(release, mergeResults(results));
	}

	private @NotNull ReleaseEnrichmentResult executeEnrichment(@NotNull ReleaseWs2 release,
															   @NotNull RelationWs2 relation,
															   @NotNull Enricher enricher) {
		Set<String> newGenres = new HashSet<>(5);
		if (enricher instanceof GenreEnricher genreEnricher) {
			Set<String> genres = genreEnricher.fetchGenres(relation);
			LOGGER.debug("Enricher '{}' found genres '{}' for release '{}'.",
				genreEnricher.getClass().getSimpleName(),
				genres,
				release.getId());

			newGenres.addAll(genres);
		}

		return new ReleaseEnrichmentResult(newGenres);
	}

	private @NotNull ReleaseEnrichmentResult mergeResults(@NotNull Collection<ReleaseEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseEnrichmentResult::genres)
			.collect(Collectors.toSet()), GENRE_BEST_PERCENTILE);

		return new ReleaseEnrichmentResult(newGenres);
	}

	private void updateEntity(@NotNull ReleaseWs2 release, @NotNull ReleaseEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			ReleaseGroupWs2 releaseGroup = release.getReleaseGroup();
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
		return DataType.RELEASE;
	}

	private static record ReleaseEnrichmentResult(Set<String> genres) {
	}

}
