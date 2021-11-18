package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.IncludesWs2;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.webservice.WebService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class MusicbrainzEditService {

	@Value("${musicbrainz-enricher.dry-run}")
	private boolean dryRun;

	private final WebService webService;
	private final MusicbrainzBucketProvider musicbrainzBucketProvider;
	private final BucketService bucketService;

	MusicbrainzEditService(@Qualifier("musicbrainzWebService") WebService webService,
						   MusicbrainzBucketProvider musicbrainzBucketProvider,
						   BucketService bucketService) {
		this.webService = webService;
		this.musicbrainzBucketProvider = musicbrainzBucketProvider;
		this.bucketService = bucketService;
	}

	public void addReleaseGroupUserTags(@NotNull UUID mbid, @NotNull Set<String> tags) throws MBWS2Exception {
		if (dryRun) {
			return;
		}

		bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

		ReleaseGroup releaseGroup = new ReleaseGroup();
		releaseGroup.setQueryWs(webService);

		IncludesWs2 includesWs2 = new ReleaseGroupIncludesWs2();
		// Note that we do not include user tags.
		// Doing so would make us re-submit user tags that were created previously.
		releaseGroup.setIncludes(includesWs2);
		releaseGroup.lookUp(mbid.toString());

		releaseGroup.AddTags(tags.toArray(new String[0]));
	}

}
