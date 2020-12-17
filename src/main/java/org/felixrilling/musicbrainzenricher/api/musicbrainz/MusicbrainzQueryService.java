package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MusicbrainzQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzQueryService.class);

    private final MusicbrainzApiService musicbrainzApiService;
    private final MusicbrainzBucketProvider musicbrainzBucketProvider;
    private final BucketService bucketService;

    MusicbrainzQueryService(MusicbrainzApiService musicbrainzApiService, MusicbrainzBucketProvider musicbrainzBucketProvider, BucketService bucketService) {
        this.musicbrainzApiService = musicbrainzApiService;
        this.musicbrainzBucketProvider = musicbrainzBucketProvider;
        this.bucketService = bucketService;
    }

    public void queryReleases(@NotNull String query, @NotNull ReleaseIncludesWs2 includes, @NotNull Consumer<String> mbidConsumer) {
        Release release = new Release();
        release.setQueryWs(musicbrainzApiService.createWebService());

        release.setIncludes(includes);

        release.search(query);
        logger.info("Starting search for releases by query '{}'.", query);

        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
        try {
            release.getFirstSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getRelease().getId()));

            while (release.hasMore()) {
                bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
                release.getNextSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getRelease().getId()));
            }
        } catch (MBWS2Exception e) {
            throw new QueryException("Could not query releases.", e);
        }
    }

    public void queryReleaseGroups(@NotNull String query, @NotNull ReleaseGroupIncludesWs2 includes, @NotNull Consumer<String> mbidConsumer) {
        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzApiService.createWebService());

        releaseGroup.setIncludes(includes);

        releaseGroup.search(query);
        logger.info("Starting search for release groups by query '{}'.", query);

        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
        releaseGroup.getFirstSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getReleaseGroup().getId()));

        while (releaseGroup.hasMore()) {
            bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
            releaseGroup.getNextSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getReleaseGroup().getId()));
        }
    }


}
