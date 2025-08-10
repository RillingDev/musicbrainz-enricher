package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.Enricher;
import dev.rilling.musicbrainzenricher.util.MergeUtils;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

	public DataType getDataType() {
		return DataType.RELEASE;
	}

	@Override

	protected Optional<ReleaseWs2> fetchEntity(UUID mbid) {
		ReleaseIncludesWs2 includes = new ReleaseIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);
		includes.setReleaseGroups(true);

		try {
			return musicbrainzLookupService.lookUpRelease(mbid, includes);
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not query the release '{}'.", mbid, e);
			return Optional.empty();
		}
	}

	@Override

	protected Collection<RelationWs2> extractRelations(ReleaseWs2 entity) {
		return entity.getRelationList().getRelations();
	}

	@Override

	protected ReleaseEnrichmentResult enrich(ReleaseWs2 entity,
											 RelationWs2 relation,
											 Enricher enricher) {
		LOGGER.debug("Starting enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);
		Set<String> genres = enricher.fetchGenres(relation);
		LOGGER.debug("Enricher {} found genres '{}' for '{}'.",
			enricher.getClass().getSimpleName(),
			genres,
			relation);

		LOGGER.debug("Completed enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);
		return new ReleaseEnrichmentResult(genres);
	}

	@Override

	protected ReleaseEnrichmentResult mergeResults(Collection<ReleaseEnrichmentResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(ReleaseEnrichmentResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);

		return new ReleaseEnrichmentResult(newGenres);
	}

	@Override
	protected void updateEntity(ReleaseWs2 entity, ReleaseEnrichmentResult result) {
		if (!result.genres().isEmpty()) {
			ReleaseGroupWs2 releaseGroup = entity.getReleaseGroup();
			LOGGER.info("Submitting new tags '{}' for the release group '{}'.", result.genres(), releaseGroup.getId());
			musicbrainzEditController.submitReleaseGroupUserTags(releaseGroup, result.genres());
		}
	}

	protected record ReleaseEnrichmentResult(Set<String> genres) {
	}

}
