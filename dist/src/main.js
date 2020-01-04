"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const MusicbrainzDatabaseService_1 = require("./api/musicbrainz/MusicbrainzDatabaseService");
const chevron_1 = require("./chevron");
const ArtistEnrichmentService_1 = require("./enrichment/artist/ArtistEnrichmentService");
const ProposedEditService_1 = require("./enrichment/ProposedEditService");
const logger_1 = require("./logger");
//Const logger = rootLogger.child({ target: "main" });
chevron_1.chevron.registerInjectable({
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
}, { name: "musicbrainzConfig" });
const musicbrainzDatabaseService = chevron_1.chevron.getInjectableInstance(MusicbrainzDatabaseService_1.MusicbrainzDatabaseService);
const artistEnrichmentService = chevron_1.chevron.getInjectableInstance(ArtistEnrichmentService_1.ArtistEnrichmentService);
const proposedEditService = chevron_1.chevron.getInjectableInstance(ProposedEditService_1.ProposedEditService);
artistEnrichmentService
    .enrich("95e27e73-7863-4d01-b3d4-214bcafe3688")
    .then(edits => {
    for (const edit of edits) {
        logger_1.rootLogger.info(proposedEditService.stringifyProposedArtistEdit(edit));
    }
})
    // eslint-disable-next-line @typescript-eslint/unbound-method
    .catch(console.error);
musicbrainzDatabaseService
    .searchArtist({
    type: "person"
}, 
// eslint-disable-next-line @typescript-eslint/unbound-method
artist => artistEnrichmentService.enrich(artist.id).then(edits => {
    for (const edit of edits) {
        logger_1.rootLogger.info(proposedEditService.stringifyProposedArtistEdit(edit));
    }
}))
    // eslint-disable-next-line @typescript-eslint/unbound-method
    .catch(console.error);
