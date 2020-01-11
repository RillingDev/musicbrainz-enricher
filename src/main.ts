import { MusicbrainzDatabaseService } from "./api/musicbrainz/MusicbrainzDatabaseService";
import { chevron } from "./chevron";
import { initConfig } from "./config.js";
import { ArtistEnrichmentService } from "./enrichment/artist/ArtistEnrichmentService";
import { ProposedEditService } from "./enrichment/ProposedEditService";
import { rootLogger } from "./logger";

const main = async (): Promise<void> => {
    await initConfig();

    const musicbrainzDatabaseService: MusicbrainzDatabaseService = chevron.getInjectableInstance(
        MusicbrainzDatabaseService
    );
    const artistEnrichmentService: ArtistEnrichmentService = chevron.getInjectableInstance(
        ArtistEnrichmentService
    );
    const proposedEditService: ProposedEditService = chevron.getInjectableInstance(
        ProposedEditService
    );
    const exampleMbid = "95e27e73-7863-4d01-b3d4-214bcafe3688";

    await musicbrainzDatabaseService.searchArtist(
        {
            type: "person"
        },
        artist =>
            artistEnrichmentService.enrich(artist.id).then(edits => {
                for (const edit of edits) {
                    rootLogger.info(
                        proposedEditService.stringifyProposedArtistEdit(edit)
                    );
                }
            })
    );
};

// eslint-disable-next-line @typescript-eslint/unbound-method
main().catch(console.error);
