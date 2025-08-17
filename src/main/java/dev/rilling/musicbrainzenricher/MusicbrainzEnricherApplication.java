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
		if (args.length == 0) {
			LOGGER.info("Running in auto-query mode.");
			musicbrainzEnricherService.runInAutoQueryMode();
		} else if (args.length == 2) {
			DataType dataType = parseDataType(args[0]);
			UUID sourceMbid = UUID.fromString(args[1]);
			LOGGER.info("Running in single mode for the data type {} with MBID '{}'.", dataType, sourceMbid);
			musicbrainzEnricherService.runInSingleMode(dataType, sourceMbid);
		} else {
			throw new IllegalArgumentException("Expected either 0 or 2 parameters but found %d.".formatted(args.length));
		}

		LOGGER.debug("Shutting down.");
		// All pending tasks should be completed anyway, so a simple shutdown is enough.
		enrichmentExecutor.shutdown();
	}


	private static DataType parseDataType(String modeString) {
		return switch (modeString) {
			case "release" -> DataType.RELEASE;
			case "release-group" -> DataType.RELEASE_GROUP;
			default -> throw new IllegalArgumentException("Could not process the mode '%s'.".formatted(modeString));
		};
	}

}
