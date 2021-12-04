package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

	private final MusicbrainzEnricherService musicbrainzEnricherService;

	MusicbrainzEnricherApplication(MusicbrainzEnricherService musicbrainzEnricherService) {
		this.musicbrainzEnricherService = musicbrainzEnricherService;
	}

	public static void main(String[] args) {
		SpringApplication.run(MusicbrainzEnricherApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Expected at least 1 arguments but found none.");
		}
		if (args.length > 2) {
			throw new IllegalArgumentException("Expected at most 2 parameters but found " + args.length + ".");
		}

		DataType dataType = parseDataType(args[0]);
		if (args.length == 2) {
			UUID mbid = UUID.fromString(args[1]);
			musicbrainzEnricherService.runInSingleMode(dataType, mbid);
		} else {
			musicbrainzEnricherService.runInAutoQueryMode(dataType);
		}
	}

	@Bean("executor")
	@Scope("singleton")
	public ExecutorService executorService(Environment environment) {
		int threadPoolSize = environment.getRequiredProperty("musicbrainz-enricher.thread-pool-size", Integer.class);
		return Executors.newFixedThreadPool(threadPoolSize);
	}

	@NotNull
	private DataType parseDataType(String modeString) {
		return switch (modeString) {
			case "release" -> DataType.RELEASE;
			case "release-group" -> DataType.RELEASE_GROUP;
			default -> throw new IllegalArgumentException("Could not process mode '" + modeString + "'.");
		};
	}

}
