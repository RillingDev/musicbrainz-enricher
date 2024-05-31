package dev.rilling.musicbrainzenricher.api.musicbrainz;

import dev.rilling.musicbrainzenricher.api.LoggingBucketListener;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.musicbrainz.webservice.WebService;
import org.musicbrainz.webservice.impl.HttpClientWebServiceWs2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.util.regex.Pattern;

@Configuration
@ThreadSafe
class MusicbrainzApiConfiguration {

	private static final Pattern UNSUPPORTED_VERSION_CHARACTER_PATTERN = Pattern.compile("-");

	@Bean("musicbrainzWebService")
	WebService createWebService(Environment environment) {
		String host = environment.getRequiredProperty("musicbrainz-enricher.host");
		String applicationName = environment.getRequiredProperty("musicbrainz-enricher.name");
		String applicationVersion = environment.getRequiredProperty("musicbrainz-enricher.version");
		String applicationContact = environment.getRequiredProperty("musicbrainz-enricher.contact");
		String username = environment.getRequiredProperty("musicbrainz-enricher.musicbrainz.username");
		String password = environment.getRequiredProperty("musicbrainz-enricher.musicbrainz.password");

		HttpClientWebServiceWs2 webService = new HttpClientWebServiceWs2(applicationName,
			applicationVersion,
			applicationContact);
		String client = getClient(applicationName, applicationVersion);
		webService.setClient(client);
		webService.setUsername(username);
		webService.setPassword(password);
		webService.setHost(host);
		return webService;
	}

	private static String getClient(String applicationName, String applicationVersion) {
		// See https://musicbrainz.org/doc/MusicBrainz_API
		String adaptedApplicationVersion = UNSUPPORTED_VERSION_CHARACTER_PATTERN.matcher(applicationVersion)
			.replaceAll("_");
		return "%s-%s".formatted(applicationName, adaptedApplicationVersion);
	}

	@Bean("musicbrainzBucket")
	Bucket musicbrainzBucket() {
		// See per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting,
		// further slowed down to adapt for network fluctuations.
		Bandwidth bandwidth = Bandwidth.builder().capacity(1).refillGreedy(1, Duration.ofMillis(1500)).build();

		return Bucket.builder().addLimit(bandwidth).build().toListenable(new LoggingBucketListener("musicbrainz"));
	}
}
