package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
class MusicbrainzService {

    @Value("${musicbrainz-enricher.host}")
    private String host;

    @Value("${musicbrainz-enricher.name}")
    private String applicationName;

    @Value("${musicbrainz-enricher.version}")
    private String applicationVersion;

    @Value("${musicbrainz-enricher.contact}")
    private String applicationContact;

    @Value("${musicbrainz-enricher.musicbrainz.username}")
    private String username;

    @Value("${musicbrainz-enricher.musicbrainz.password}")
    private String password;

    @NotNull WebService createWebService() {
        HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2();
        webService.setClient(getClientName(applicationName, applicationVersion, applicationContact));
        webService.setUsername(username);
        webService.setPassword(password);
        webService.setHost(host);
        return webService;
    }

    private @NotNull String getClientName(@NotNull String applicationName, @NotNull String applicationVersion, @NotNull String applicationContact) {
        // See https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting
        // While Musicbrainz states that contact details should be added, these seem to cause issues with the Java API.
        return String.format("%s/%s", applicationName, applicationVersion);
    }
}
