package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import org.felixrilling.musicbrainzenricher.api.BucketService;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.webservice.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
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

    public void queryReleases(@NotNull String query, @NotNull ReleaseIncludesWs2 includes, @NotNull Consumer<String> mbidConsumer) {
        Release release = new Release();
        release.setQueryWs(musicbrainzService.createWebService());

        release.setIncludes(includes);

        release.search(query);

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
        releaseGroup.setQueryWs(musicbrainzService.createWebService());

        releaseGroup.setIncludes(includes);

        releaseGroup.search(query);

        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
        releaseGroup.getFirstSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getReleaseGroup().getId()));

        while (releaseGroup.hasMore()) {
            bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());
            releaseGroup.getNextSearchResultPage().forEach(releaseWs2 -> mbidConsumer.accept(releaseWs2.getReleaseGroup().getId()));
        }
    }

    public @NotNull Optional<ReleaseWs2> lookUpRelease(@NotNull String mbid, @NotNull ReleaseIncludesWs2 includes) {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        Release release = new Release();
        release.setQueryWs(musicbrainzService.createWebService());

        release.setIncludes(includes);

        try {
            return Optional.of(release.lookUp(mbid));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (MBWS2Exception e) {
            throw new QueryException("Could not look up release '" + mbid + "'.", e);
        }
    }

    public @NotNull Optional<ReleaseGroupWs2> lookUpReleaseGroup(@NotNull String mbid, @NotNull ReleaseGroupIncludesWs2 includes) {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzService.createWebService());

        releaseGroup.setIncludes(includes);

        try {
            return Optional.of(releaseGroup.lookUp(mbid));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (MBWS2Exception e) {
            throw new QueryException("Could not look up release group'" + mbid + "'.", e);
        }
    }
}
