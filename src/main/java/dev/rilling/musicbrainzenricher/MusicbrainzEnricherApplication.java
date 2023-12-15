package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.core.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

	private final MusicbrainzEnricherService musicbrainzEnricherService;
	private final ExecutorService enrichmentExecutor;

	MusicbrainzEnricherApplication(MusicbrainzEnricherService musicbrainzEnricherService, @Qualifier("enrichmentExecutor") ExecutorService enrichmentExecutor) {
		this.musicbrainzEnricherService = musicbrainzEnricherService;
		this.enrichmentExecutor = enrichmentExecutor;
	}

	public static void main(String[] args) {
		SpringApplication.run(MusicbrainzEnricherApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Expected at least 1 argument but found none.");
		}
		if (args.length > 2) {
			throw new IllegalArgumentException("Expected at most 2 parameters but found %d.".formatted(args.length));
		}

		DataType dataType = parseDataType(args[0]);
		if (args.length == 2) {
			UUID mbid = UUID.fromString(args[1]);
			LOGGER.info("Running in single mode for the data type {} with MBID '{}'.", dataType, mbid);
			musicbrainzEnricherService.runInSingleMode(dataType, mbid);
		} else {
			LOGGER.info("Running in auto-query mode for the data type {}.", dataType);
			musicbrainzEnricherService.runInAutoQueryMode(dataType);
		}

		shutdown();
	}

	private void shutdown() {
		LOGGER.debug("Shutting down.");
		// All pending tasks should be completed anyway, so a simple shutdown is enough.
		enrichmentExecutor.shutdown();
	}


	private DataType parseDataType(String modeString) {
		return switch (modeString) {
			case "release" -> DataType.RELEASE;
			case "release-group" -> DataType.RELEASE_GROUP;
			default -> throw new IllegalArgumentException("Could not process the mode '%s'.".formatted(modeString));
		};
	}

}
