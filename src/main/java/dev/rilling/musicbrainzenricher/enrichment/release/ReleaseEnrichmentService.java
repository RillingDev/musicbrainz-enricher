package dev.rilling.musicbrainzenricher.enrichment.release;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzException;
import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzLookupService;
import dev.rilling.musicbrainzenricher.core.DataType;
import dev.rilling.musicbrainzenricher.enrichment.AbstractEnrichmentService;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
public class ReleaseEnrichmentService extends AbstractEnrichmentService<ReleaseWs2> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseEnrichmentService.class);

	private final MusicbrainzLookupService musicbrainzLookupService;

	ReleaseEnrichmentService(ApplicationContext applicationContext,
							 @Qualifier("enrichmentExecutor") ExecutorService executorService,
							 MusicbrainzLookupService musicbrainzLookupService) {
		super(applicationContext, executorService);
		this.musicbrainzLookupService = musicbrainzLookupService;
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
	protected ReleaseGroupWs2 extractTargetEntity(ReleaseWs2 entity) {
		// While releases can have their own tags, we opt to put them on the release group instead.
		return entity.getReleaseGroup();
	}
}
