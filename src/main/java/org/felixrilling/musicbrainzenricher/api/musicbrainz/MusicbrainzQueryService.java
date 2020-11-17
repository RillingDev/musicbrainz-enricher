package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MusicbrainzQueryService {

    private final MusicbrainzService musicbrainzService;
    private final MusicbrainzBucketProvider musicbrainzBucketProvider;
    private final BucketService bucketService;

    MusicbrainzQueryService(MusicbrainzService musicbrainzService, MusicbrainzBucketProvider musicbrainzBucketProvider, BucketService bucketService) {
        this.musicbrainzService = musicbrainzService;
        this.musicbrainzBucketProvider = musicbrainzBucketProvider;
        this.bucketService = bucketService;
    }

    public void queryRelease(@NotNull String query, @NotNull ReleaseIncludesWs2 includes, @NotNull Consumer<ReleaseWs2> consumer) {
        Release release = new Release();
        release.setQueryWs(musicbrainzService.createWebService());

        release.setIncludes(includes);

        release.search(query);

        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
        try {
            release.getFirstSearchResultPage().forEach(releaseWs2 -> consumer.accept(releaseWs2.getRelease()));

            while (release.hasMore()) {
                bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
                release.getNextSearchResultPage().forEach(releaseWs2 -> consumer.accept(releaseWs2.getRelease()));
            }
        } catch (MBWS2Exception e) {
            throw new IllegalStateException("Could not query releases.", e);
        }
    }

    public ReleaseWs2 lookUpRelease(@NotNull String mbid, @NotNull ReleaseIncludesWs2 includes) {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        Release release = new Release();
        release.setQueryWs(musicbrainzService.createWebService());

        release.setIncludes(includes);

        try {
            return release.lookUp(mbid);
        } catch (MBWS2Exception e) {
            throw new IllegalStateException("Could not look up release.", e);
        }
    }

}
