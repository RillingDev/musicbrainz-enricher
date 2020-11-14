package org.felixrilling.musicbrainzenricher.io.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class MusicbrainzService {

    @Value("${musicbrainz.enricher.host}")
    private String host;

    @Value("${musicbrainz.enricher.name}")
    private String applicationName;

    @Value("${musicbrainz.enricher.version}")
    private String applicationVersion;

    @Value("${musicbrainz.enricher.contact}")
    private String applicationContact;

    @Value("${musicbrainz.enricher.client.mb}")
    private String client;

    @Value("${musicbrainz.enricher.username}")
    private String username;

    @Value("${musicbrainz.enricher.password}")
    private String password;

    @NotNull WebService createWebService() {
        HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2(applicationName, applicationVersion, applicationContact);
        webService.setClient(client);
        webService.setUsername(username);
        webService.setPassword(password);
        webService.setHost(host);
        return webService;
    }
}