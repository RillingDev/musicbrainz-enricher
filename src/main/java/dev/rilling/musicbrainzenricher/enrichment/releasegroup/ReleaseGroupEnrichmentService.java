package dev.rilling.musicbrainzenricher.enrichment.releasegroup;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
import dev.rilling.musicbrainzenricher.enrichment.Enricher;
import dev.rilling.musicbrainzenricher.util.MergeUtils;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.model.RelationWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
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
public class ReleaseGroupEnrichmentService extends AbstractEnrichmentService<ReleaseGroupWs2> {

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

	public DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}

	@Override

	protected Optional<ReleaseGroupWs2> fetchEntity(UUID mbid) {
		ReleaseGroupIncludesWs2 includes = new ReleaseGroupIncludesWs2();
		includes.setUrlRelations(true);
		includes.setTags(true);
		includes.setUserTags(true);

		try {
			return musicbrainzLookupService.lookUpReleaseGroup(mbid, includes);
		} catch (MusicbrainzException e) {
			LOGGER.error("Could not query the release-group '{}'.", mbid, e);
			return Optional.empty();
		}
	}

	@Override

	protected Collection<RelationWs2> extractRelations(ReleaseGroupWs2 entity) {
		return entity.getRelationList().getRelations();
	}

	@Override

	protected EnricherResult enrich(ReleaseGroupWs2 entity,
									RelationWs2 relation,
									Enricher enricher) {
		LOGGER.debug("Starting enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);
		Set<String> genres = enricher.fetchGenres(relation);
		LOGGER.debug("Enricher {} found genres '{}' for '{}'.",
			enricher.getClass().getSimpleName(),
			genres,
			relation);

		LOGGER.debug("Completed enricher {} for '{}'.", enricher.getClass().getSimpleName(), relation);

		return new EnricherResult(genres);
	}

	protected void updateEntity(ReleaseGroupWs2 entity, Set<EnricherResult> results) {
		Set<String> newGenres = MergeUtils.getMostCommon(results.stream()
			.map(EnricherResult::genres)
			.collect(Collectors.toSet()), MIN_GENRE_USAGE);
		if (!results.isEmpty()) {
			LOGGER.info("Submitting new tags '{}' for the release group '{}'.", newGenres, entity.getId());
			musicbrainzEditController.submitReleaseGroupUserTags(entity, newGenres);
		}
	}
}
