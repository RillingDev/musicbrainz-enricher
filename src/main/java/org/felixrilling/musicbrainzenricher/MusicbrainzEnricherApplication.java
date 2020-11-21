package org.felixrilling.musicbrainzenricher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

    private final EnrichmentService enrichmentService;
    private final Environment environment;

    MusicbrainzEnricherApplication(EnrichmentService enrichmentService, Environment environment) {
        this.enrichmentService = enrichmentService;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(MusicbrainzEnricherApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected at least 1 arguments but found none.");
        }
        DataType dataType = parseMode(args[0]);

        try {
            if (Arrays.asList(environment.getActiveProfiles()).contains("musicbrainzLocalDb")) {
                enrichmentService.runInDumpMode(dataType);
            } else {
                if (args.length < 2) {
                    throw new IllegalArgumentException("Expected a second arguments but found none.");
                }
                String query = args[1];
                enrichmentService.runInQueryMode(dataType, query);
            }
        } catch (Exception e) {
            logger.error("Unexpected error.", e);
        }
    }


    private DataType parseMode(String modeString) {
        switch (modeString) {
            case "release":
                return DataType.RELEASE;
            default:
                throw new IllegalArgumentException("Could not process mode '" + modeString + "'.");
        }
    }

}
