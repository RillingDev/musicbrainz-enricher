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
		if (args.length != 1 && args.length != 3) {
			throw new IllegalArgumentException("Expected either 1 or 3 parameters but found %d.".formatted(args.length));
		} else {
			String command = args[0];
			switch (command) {
				case "gather" -> {
					if (args.length == 3) {
						// This mode is for debugging. Make sure to manually clean the history/result tables.
						DataType dataType = parseDataType(args[1]);
						UUID sourceMbid = UUID.fromString(args[2]);
						LOGGER.info("Running in gather mode for the data type {} with MBID '{}'.", dataType, sourceMbid);
						musicbrainzEnricherService.gather(dataType, sourceMbid);
					} else {
						LOGGER.info("Running in gather mode.");
						musicbrainzEnricherService.gatherAll();
					}
				}
				case "submit" -> {
					LOGGER.info("Running in submit mode.");
					musicbrainzEnricherService.submitTags();
				}
				default -> throw new IllegalArgumentException("Could not process the command '%s'.".formatted(command));
			}
		}

		LOGGER.info("Shutting down.");
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
