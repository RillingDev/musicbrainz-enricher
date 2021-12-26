package dev.rilling.musicbrainzenricher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

	@Bean("enrichmentExecutor")
	@Scope("singleton")
	public ExecutorService enrichmentExecutor(Environment environment) {
		int threadPoolSize = environment.getRequiredProperty("musicbrainz-enricher.enrichment-thread-pool-size",
			Integer.class);
		return Executors.newFixedThreadPool(threadPoolSize);
	}

	@Bean("submissionExecutor")
	@Scope("singleton")
	public ExecutorService submissionExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	@Bean("utcClock")
	public Clock utcClockSupplier() {
		return Clock.systemUTC();
	}

}
