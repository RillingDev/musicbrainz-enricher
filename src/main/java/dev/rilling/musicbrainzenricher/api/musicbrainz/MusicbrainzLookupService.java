package dev.rilling.musicbrainzenricher.api.musicbrainz;

import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.webservice.ResourceNotFoundException;
import org.musicbrainz.webservice.WebService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@ThreadSafe
public class MusicbrainzLookupService {

	private final WebService webService;
	private final Bucket bucket;

	MusicbrainzLookupService(@Qualifier("musicbrainzWebService") WebService webService,
							 @Qualifier("musicbrainzBucket") Bucket bucket) {
		this.webService = webService;
		this.bucket = bucket;
	}


	public Optional<ReleaseWs2> lookUpRelease(UUID mbid, ReleaseIncludesWs2 includes)
		throws MusicbrainzException {
		bucket.asBlocking().consumeUninterruptibly(1);

		Release release = new Release();
		release.setQueryWs(webService);

		release.setIncludes(includes);

		try {
			return Optional.of(release.lookUp(mbid.toString()));
		} catch (ResourceNotFoundException e) {
			return Optional.empty();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not look up the release '%s'.".formatted(mbid), e);
		}
	}


	public Optional<ReleaseGroupWs2> lookUpReleaseGroup(UUID mbid, ReleaseGroupIncludesWs2 includes)
		throws MusicbrainzException {
		bucket.asBlocking().consumeUninterruptibly(1);

		ReleaseGroup releaseGroup = new ReleaseGroup();
		releaseGroup.setQueryWs(webService);

		releaseGroup.setIncludes(includes);

		try {
			return Optional.of(releaseGroup.lookUp(mbid.toString()));
		} catch (ResourceNotFoundException e) {
			return Optional.empty();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not look up the release group '%s'.".formatted(mbid), e);
		}
	}

}
