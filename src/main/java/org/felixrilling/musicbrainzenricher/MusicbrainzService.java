package org.felixrilling.musicbrainzenricher;

import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Artist;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.includes.ArtistIncludesWs2;
import org.musicbrainz.includes.IncludesWs2;
import org.musicbrainz.includes.ReleaseGroupIncludesWs2;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.musicbrainz.model.entity.ArtistWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class MusicbrainzService {


    @Value("${musicbrainz.enricher.host}")
    private String host;

    @Value("${musicbrainz.enricher.name}")
    private String applicationName;

    @Value("${musicbrainz.enricher.version}")
    private String applicationVersion;

    @Value("${musicbrainz.enricher.contact}")
    private String applicationContact;

    @Value("${musicbrainz.enricher.client}")
    private String client;

    @Value("${musicbrainz.enricher.username}")
    private String username;

    @Value("${musicbrainz.enricher.password}")
    private String password;

    public ArtistWs2 lookUpArtist(String mbid, ArtistIncludesWs2 includes) throws MBWS2Exception {
        Artist artist = new Artist();
        artist.setQueryWs(createWebService());

        artist.setIncludes(includes);

        return artist.lookUp(mbid);
    }

    public ReleaseWs2 lookUpRelease(String mbid, ReleaseIncludesWs2 includes) throws MBWS2Exception {
        Release release = new Release();
        release.setQueryWs(createWebService());

        release.setIncludes(includes);

        return release.lookUp(mbid);
    }

    public void addReleaseGroupUserTags(String mbid, Set<String> tags) throws MBWS2Exception {
        ReleaseGroup releaseGroup = new ReleaseGroup();
        releaseGroup.setQueryWs(createWebService());

        IncludesWs2 includesWs2 = new ReleaseGroupIncludesWs2();
        includesWs2.setUserTags(true);
        releaseGroup.setIncludes(includesWs2);

        releaseGroup.lookUp(mbid);

        releaseGroup.AddTags(tags.toArray(new String[0]));
    }

    public WebService createWebService() {
        HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2(applicationName, applicationVersion, applicationContact);
        webService.setClient(client);
        webService.setUsername(username);
        webService.setPassword(password);
        webService.setHost(host);
        return webService;
    }
}
