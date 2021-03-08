package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.IncludesWs2;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class MusicbrainzEditService {

    private final MusicbrainzApiService musicbrainzApiService;
    private final MusicbrainzBucketProvider musicbrainzBucketProvider;
    private final BucketService bucketService;

    MusicbrainzEditService(MusicbrainzApiService musicbrainzApiService, MusicbrainzBucketProvider musicbrainzBucketProvider, BucketService bucketService) {
        this.musicbrainzApiService = musicbrainzApiService;
        this.musicbrainzBucketProvider = musicbrainzBucketProvider;
        this.bucketService = bucketService;
    }

    public void addReleaseGroupUserTags(@NotNull UUID mbid, @NotNull Set<String> tags) throws MBWS2Exception {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzApiService.createWebService());

        IncludesWs2 includesWs2 = new ReleaseGroupIncludesWs2();
        includesWs2.setUserTags(true);
        releaseGroup.setIncludes(includesWs2);

        releaseGroup.lookUp(mbid.toString());

        releaseGroup.AddTags(tags.toArray(new String[0]));
    }

}
