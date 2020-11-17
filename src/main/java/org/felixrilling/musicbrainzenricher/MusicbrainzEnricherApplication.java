package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.release.ReleaseEnricherService;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MusicbrainzEnricherApplication.class);

    private final ReleaseEnricherService releaseEnricherService;
    private final MusicbrainzQueryService musicbrainzQueryService;

    MusicbrainzEnricherApplication(ReleaseEnricherService releaseEnricherService, MusicbrainzQueryService musicbrainzQueryService) {
        this.releaseEnricherService = releaseEnricherService;
        this.musicbrainzQueryService = musicbrainzQueryService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MusicbrainzEnricherApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected exactly 2 arguments but found " + args.length + ".");
        }
        String mode = args[0];
        String query = args[1];

        switch (mode) {
            case "release":
                enrichRelease(query);
                break;
            default:
                throw new IllegalArgumentException("Could not process mode '" + mode + "'.");
        }
    }

    private void enrichRelease(String query) {
        musicbrainzQueryService.queryRelease(query, new ReleaseIncludesWs2(), releaseWs2 -> {
            try {
                releaseEnricherService.enrichRelease(releaseWs2.getId());
            } catch (Exception e) {
                logger.error("Could not enrich releases.", e);
            }
        });
    }
}
