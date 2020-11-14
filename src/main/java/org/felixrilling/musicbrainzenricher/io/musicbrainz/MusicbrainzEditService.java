package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import org.felixrilling.musicbrainzenricher.io.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.IncludesWs2;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MusicbrainzEditService {

    private final MusicbrainzService musicbrainzService;
    private final MusicbrainzBucketProvider musicbrainzBucketProvider;
    private final BucketService bucketService;

    MusicbrainzEditService(MusicbrainzService musicbrainzService, MusicbrainzBucketProvider musicbrainzBucketProvider, BucketService bucketService) {
        this.musicbrainzService = musicbrainzService;
        this.musicbrainzBucketProvider = musicbrainzBucketProvider;
        this.bucketService = bucketService;
    }

    public void addReleaseGroupUserTags(@NotNull String mbid, @NotNull Set<String> tags) throws MBWS2Exception {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzService.createWebService());

        IncludesWs2 includesWs2 = new ReleaseGroupIncludesWs2();
        includesWs2.setUserTags(true);
        releaseGroup.setIncludes(includesWs2);

        releaseGroup.lookUp(mbid);

        releaseGroup.AddTags(tags.toArray(new String[0]));
    }

}