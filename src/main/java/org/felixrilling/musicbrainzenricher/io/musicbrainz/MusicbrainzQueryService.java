package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Artist;
import org.musicbrainz.controller.Release;
import org.musicbrainz.includes.ArtistIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.entity.ArtistWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.springframework.stereotype.Service;

@Service
public class MusicbrainzQueryService {

    private final MusicbrainzService musicbrainzService;

    MusicbrainzQueryService(MusicbrainzService musicbrainzService) {
        this.musicbrainzService = musicbrainzService;
    }

    public ArtistWs2 lookUpArtist(@NotNull String mbid, @NotNull ArtistIncludesWs2 includes) throws MBWS2Exception {
        Artist artist = new Artist();
        artist.setQueryWs(musicbrainzService.createWebService());

        artist.setIncludes(includes);

        return artist.lookUp(mbid);
    }

    public ReleaseWs2 lookUpRelease(@NotNull String mbid, @NotNull ReleaseIncludesWs2 includes) throws MBWS2Exception {
        Release release = new Release();
        release.setQueryWs(musicbrainzService.createWebService());

        release.setIncludes(includes);

        return release.lookUp(mbid);
    }

}
