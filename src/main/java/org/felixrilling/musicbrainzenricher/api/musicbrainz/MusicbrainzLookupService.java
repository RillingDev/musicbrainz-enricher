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
import java.util.UUID;

@Service
public class MusicbrainzLookupService {

    private final MusicbrainzApiService musicbrainzApiService;
    private final MusicbrainzBucketProvider musicbrainzBucketProvider;
    private final BucketService bucketService;

    MusicbrainzLookupService(MusicbrainzApiService musicbrainzApiService, MusicbrainzBucketProvider musicbrainzBucketProvider, BucketService bucketService) {
        this.musicbrainzApiService = musicbrainzApiService;
        this.musicbrainzBucketProvider = musicbrainzBucketProvider;
        this.bucketService = bucketService;
    }

    public @NotNull Optional<ReleaseWs2> lookUpRelease(@NotNull UUID mbid, @NotNull ReleaseIncludesWs2 includes) {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        Release release = new Release();
        release.setQueryWs(musicbrainzApiService.createWebService());

        release.setIncludes(includes);

        try {
            return Optional.of(release.lookUp(mbid.toString()));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (MBWS2Exception e) {
            throw new QueryException("Could not look up release '" + mbid + "'.", e);
        }
    }

    public @NotNull Optional<ReleaseGroupWs2> lookUpReleaseGroup(@NotNull UUID mbid, @NotNull ReleaseGroupIncludesWs2 includes) {
        bucketService.consumeSingleBlocking(musicbrainzBucketProvider.getBucket());

        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzApiService.createWebService());

        releaseGroup.setIncludes(includes);

        try {
            return Optional.of(releaseGroup.lookUp(mbid.toString()));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (MBWS2Exception e) {
            throw new QueryException("Could not look up release group'" + mbid + "'.", e);
        }
    }

    private static class QueryException extends RuntimeException {
        QueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
