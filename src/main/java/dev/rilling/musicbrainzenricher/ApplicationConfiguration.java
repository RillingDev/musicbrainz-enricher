package dev.rilling.musicbrainzenricher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

	@Bean("enrichmentExecutor")
	public ExecutorService enrichmentExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
