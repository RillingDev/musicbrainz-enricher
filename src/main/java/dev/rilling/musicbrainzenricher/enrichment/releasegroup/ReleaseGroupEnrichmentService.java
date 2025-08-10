package dev.rilling.musicbrainzenricher.enrichment.releasegroup;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
public class ReleaseGroupEnrichmentService extends AbstractEnrichmentService<ReleaseGroupWs2> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupEnrichmentService.class);

	private final MusicbrainzLookupService musicbrainzLookupService;

	ReleaseGroupEnrichmentService(ApplicationContext applicationContext,
								  @Qualifier("enrichmentExecutor") ExecutorService executorService,
								  MusicbrainzLookupService musicbrainzLookupService,
								  MusicbrainzEditController musicbrainzEditController) {
		super(applicationContext, executorService, musicbrainzEditController);
		this.musicbrainzLookupService = musicbrainzLookupService;
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
	protected ReleaseGroupWs2 extractTargetEntity(ReleaseGroupWs2 entity) {
		return entity;
	}
}
