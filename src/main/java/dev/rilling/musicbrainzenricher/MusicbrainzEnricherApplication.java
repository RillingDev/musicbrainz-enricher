package dev.rilling.musicbrainzenricher;

import dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController;
import dev.rilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

	private final MusicbrainzEnricherService musicbrainzEnricherService;
	private final MusicbrainzEditController musicbrainzEditController;
	private final ExecutorService enrichmentExecutor;
	private final ExecutorService submissionExecutor;

	MusicbrainzEnricherApplication(MusicbrainzEnricherService musicbrainzEnricherService,
								   MusicbrainzEditController musicbrainzEditController,
								   @Qualifier("enrichmentExecutor") ExecutorService enrichmentExecutor,
								   @Qualifier("submissionExecutor") ExecutorService submissionExecutor) {
		this.musicbrainzEnricherService = musicbrainzEnricherService;
		this.musicbrainzEditController = musicbrainzEditController;
		this.enrichmentExecutor = enrichmentExecutor;
		this.submissionExecutor = submissionExecutor;
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
			throw new IllegalArgumentException("Expected at most 2 parameters but found %d.".formatted(args.length));
		}

		DataType dataType = parseDataType(args[0]);
		if (args.length == 2) {
			UUID mbid = UUID.fromString(args[1]);
			musicbrainzEnricherService.runInSingleMode(dataType, mbid);
		} else {
			musicbrainzEnricherService.runInAutoQueryMode(dataType);
		}

		try {
			shutdown();
		} catch (InterruptedException e) {
			// Can be ignored during application shutdown
		}
	}

	private void shutdown() throws InterruptedException {
		LOGGER.debug("Shutting down");

		try {
			musicbrainzEditController.flush().get();
		} catch (ExecutionException e) {
			LOGGER.error("Failed to flush edits.", e);
		}

		enrichmentExecutor.shutdown();
		submissionExecutor.shutdown();

		if (!enrichmentExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
			LOGGER.warn("Exceeded timeout for enrichment executor termination.");
		}
		if (!submissionExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
			LOGGER.warn("Exceeded timeout for submission executor termination.");
		}
	}

	@NotNull
	private DataType parseDataType(String modeString) {
		return switch (modeString) {
			case "release" -> DataType.RELEASE;
			case "release-group" -> DataType.RELEASE_GROUP;
			default -> throw new IllegalArgumentException("Could not process mode '%s'.".formatted(modeString));
		};
	}

}
