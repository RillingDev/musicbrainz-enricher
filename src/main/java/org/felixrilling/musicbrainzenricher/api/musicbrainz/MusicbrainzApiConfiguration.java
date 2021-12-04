package org.felixrilling.musicbrainzenricher.api.musicbrainz;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ThreadSafe
class MusicbrainzApiConfiguration {

	@Bean("musicbrainzWebService")
	@NotNull WebService createWebService(Environment environment) {
		String host = environment.getRequiredProperty("musicbrainz-enricher.host");
		String applicationName = environment.getRequiredProperty("musicbrainz-enricher.name");
		String applicationVersion = environment.getRequiredProperty("musicbrainz-enricher.version");
		String applicationContact = environment.getRequiredProperty("musicbrainz-enricher.contact");
		String username = environment.getRequiredProperty("musicbrainz-enricher.musicbrainz.username");
		String password = environment.getRequiredProperty("musicbrainz-enricher.musicbrainz.password");

		HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2(applicationName,
			applicationVersion,
			applicationContact);
		// See org.musicbrainz.webservice.DefaultWebServiceWs2.client
		webService.setClient(String.format("%s-%s", applicationName, applicationVersion));
		webService.setUsername(username);
		webService.setPassword(password);
		webService.setHost(host);
		return webService;
	}

}
