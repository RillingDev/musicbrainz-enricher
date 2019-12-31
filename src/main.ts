import { IArtist } from "musicbrainz-api";
import { DiscogsDatabaseService } from "./api/discogs/DiscogsDatabaseService";
import { MusicbrainzDatabaseService } from "./api/musicbrainz/MusicbrainzDatabaseService";
import { chevron } from "./chevron";
import { ArtistEnrichmentService } from "./enrichment/ArtistEnrichmentService";
import { rootLogger } from "./logger";

const logger = rootLogger.child({ target: "main" });

chevron.registerInjectable(
    {
        // MusicBrainz bot account username & password (optional)
        botAccount: {
            username: "unmasked_bot",
            password: process.env.MUSICBRAINZ_PASSWORD
        },

        // API base URL, default: 'https://musicbrainz.org' (optional)
        baseUrl: "https://musicbrainz.org/",

        appName: "data-enrichtment-bot",
        appVersion: "0.1.0",

        // Your e-mail address, required for submitting ISRCs
        appContactInfo: "contact@rilling.dev"
    },
    { name: "musicbrainzConfig" }
);

const musicbrainzDatabaseService: MusicbrainzDatabaseService = chevron.getInjectableInstance(
    MusicbrainzDatabaseService
);
const artistEnrichmentService: ArtistEnrichmentService = chevron.getInjectableInstance(
    ArtistEnrichmentService
);
// ArtistEnrichmentService
//     .enrich("488adff4-0b5c-41c9-aa8c-570aeae15737")
//     .catch(logger.error);
musicbrainzDatabaseService
    .search({
        type: "person"
    })
    .then(result => {
        for (const artist of result.artists) {
            artistEnrichmentService.enrich(artist.id).catch(logger.error);
        }
    })
    .catch(logger.error);
