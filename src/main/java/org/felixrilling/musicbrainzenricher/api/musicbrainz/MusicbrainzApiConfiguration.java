package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ThreadSafe
class MusicbrainzApiConfiguration {

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

	@Bean("musicbrainzWebService")
	@NotNull WebService createWebService() {
		HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2(applicationName,
			applicationVersion,
			applicationContact);
		webService.setClient(getClientName(applicationName, applicationVersion));
		webService.setUsername(username);
		webService.setPassword(password);
		webService.setHost(host);
		return webService;
	}

	@NotNull
	private static String getClientName(@NotNull String applicationName, @NotNull String applicationVersion) {
		// See org.musicbrainz.webservice.DefaultWebServiceWs2.client
		return String.format("%s-%s", applicationName, applicationVersion);
	}
}
