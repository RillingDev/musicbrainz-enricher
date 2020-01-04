import { MusicbrainzDatabaseService } from "./api/musicbrainz/MusicbrainzDatabaseService";
import { chevron } from "./chevron";
import { ArtistEnrichmentService } from "./enrichment/artist/ArtistEnrichmentService";
import { ProposedEditService } from "./enrichment/ProposedEditService";
import { rootLogger } from "./logger";

//Const logger = rootLogger.child({ target: "main" });

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
const proposedEditService: ProposedEditService = chevron.getInjectableInstance(
    ProposedEditService
);
artistEnrichmentService
    .enrich("95e27e73-7863-4d01-b3d4-214bcafe3688")
    .then(edits => {
        for (const edit of edits) {
            rootLogger.info(
                proposedEditService.stringifyProposedArtistEdit(edit)
            );
        }
    })
    // eslint-disable-next-line @typescript-eslint/unbound-method
    .catch(console.error);

musicbrainzDatabaseService
    .searchArtist(
        {
            type: "person"
        },
        // eslint-disable-next-line @typescript-eslint/unbound-method
        artist =>
            artistEnrichmentService.enrich(artist.id).then(edits => {
                for (const edit of edits) {
                    rootLogger.info(
                        proposedEditService.stringifyProposedArtistEdit(edit)
                    );
                }
            })
    )
    // eslint-disable-next-line @typescript-eslint/unbound-method
    .catch(console.error);
