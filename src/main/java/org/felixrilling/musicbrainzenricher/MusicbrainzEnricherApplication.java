package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

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
        try {
            if (args.length == 2) {
                UUID mbid = UUID.fromString(args[1]);
                musicbrainzEnricherService.runInSingleMode(dataType, mbid);
            } else {
                musicbrainzEnricherService.runInAutoQueryMode(dataType);
            }
        } catch (Exception e) {
            logger.error("Unexpected error.", e);
        }
    }


    private DataType parseDataType(String modeString) {
        switch (modeString) {
            case "release":
                return DataType.RELEASE;
            case "release-group":
                return DataType.RELEASE_GROUP;
            default:
                throw new IllegalArgumentException("Could not process mode '" + modeString + "'.");
        }
    }

}
