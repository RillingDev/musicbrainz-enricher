package org.felixrilling.musicbrainzenricher;

import org.felixrilling.musicbrainzenricher.io.musicbrainz.MusicbrainzQueryService;
import org.felixrilling.musicbrainzenricher.release.ReleaseEnricherService;
import org.musicbrainz.includes.ReleaseIncludesWs2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MusicbrainzEnricherApplication implements CommandLineRunner {

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
    public void run(String... args) throws Exception {
        musicbrainzQueryService.queryRelease("a", new ReleaseIncludesWs2(), releaseWs2 -> {
            try {
                releaseEnricherService.enrichRelease(releaseWs2.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
