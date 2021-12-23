package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.api.BucketService;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
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
	private final MusicbrainzBucketProvider musicbrainzBucketProvider;
	private final BucketService bucketService;

	MusicbrainzLookupService(@Qualifier("musicbrainzWebService") WebService webService,
							 MusicbrainzBucketProvider musicbrainzBucketProvider,
							 BucketService bucketService) {
		this.webService = webService;
		this.musicbrainzBucketProvider = musicbrainzBucketProvider;
		this.bucketService = bucketService;
	}

	@NotNull
	public Optional<ReleaseWs2> lookUpRelease(@NotNull UUID mbid, @NotNull ReleaseIncludesWs2 includes)
		throws MusicbrainzException {
		bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

		Release release = new Release();
		release.setQueryWs(webService);

		release.setIncludes(includes);

		try {
			return Optional.of(release.lookUp(mbid.toString()));
		} catch (ResourceNotFoundException e) {
			return Optional.empty();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not look up release '%s'.".formatted(mbid), e);
		}
	}

	@NotNull
	public Optional<ReleaseGroupWs2> lookUpReleaseGroup(@NotNull UUID mbid, @NotNull ReleaseGroupIncludesWs2 includes)
		throws MusicbrainzException {
		bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

		ReleaseGroup releaseGroup = new ReleaseGroup();
		releaseGroup.setQueryWs(webService);

		releaseGroup.setIncludes(includes);

		try {
			return Optional.of(releaseGroup.lookUp(mbid.toString()));
		} catch (ResourceNotFoundException e) {
			return Optional.empty();
		} catch (MBWS2Exception e) {
			throw new MusicbrainzException("Could not look up release group'%s'.".formatted(mbid), e);
		}
	}

}
