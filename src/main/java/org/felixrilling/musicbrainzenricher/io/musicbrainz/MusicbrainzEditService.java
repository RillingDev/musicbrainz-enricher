package org.felixrilling.musicbrainzenricher.io.musicbrainz;

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

    MusicbrainzEditService(MusicbrainzService musicbrainzService) {
        this.musicbrainzService = musicbrainzService;
    }

    public void addReleaseGroupUserTags(@NotNull String mbid, @NotNull Set<String> tags) throws MBWS2Exception {
        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(musicbrainzService.createWebService());

        IncludesWs2 includesWs2 = new ReleaseGroupIncludesWs2();
        includesWs2.setUserTags(true);
        releaseGroup.setIncludes(includesWs2);

        releaseGroup.lookUp(mbid);

        releaseGroup.AddTags(tags.toArray(new String[0]));
    }

}
