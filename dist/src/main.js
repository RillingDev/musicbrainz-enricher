"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const MusicbrainzDatabaseService_1 = require("./api/musicbrainz/MusicbrainzDatabaseService");
const chevron_1 = require("./chevron");
const ProposedEditService_js_1 = require("./edit/ProposedEditService.js");
const ArtistEnrichmentService_1 = require("./enrichment/artist/ArtistEnrichmentService");
const logger_1 = require("./logger");
const main = async () => {
    const musicbrainzDatabaseService = chevron_1.chevron.getInjectableInstance(MusicbrainzDatabaseService_1.MusicbrainzDatabaseService);
    const artistEnrichmentService = chevron_1.chevron.getInjectableInstance(ArtistEnrichmentService_1.ArtistEnrichmentService);
    const proposedEditService = chevron_1.chevron.getInjectableInstance(ProposedEditService_js_1.ProposedEditService);
    const exampleMbid = "95e27e73-7863-4d01-b3d4-214bcafe3688";
    await musicbrainzDatabaseService.searchArtist({
        type: "person"
    }, artist => artistEnrichmentService.enrich(artist.id).then(edits => {
        for (const edit of edits) {
            logger_1.rootLogger.info(proposedEditService.stringifyProposedArtistEdit(edit));
        }
    }));
};
// eslint-disable-next-line @typescript-eslint/unbound-method
main().catch(console.error);
