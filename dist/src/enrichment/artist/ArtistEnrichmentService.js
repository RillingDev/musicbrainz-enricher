"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var ArtistEnrichmentService_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const MusicbrainzDatabaseService_1 = require("../../api/musicbrainz/MusicbrainzDatabaseService");
const chevron_1 = require("../../chevron");
const logger_1 = require("../../logger");
const DiscogsArtistEnricher_js_1 = require("./enricher/DiscogsArtistEnricher.js");
let ArtistEnrichmentService = ArtistEnrichmentService_1 = class ArtistEnrichmentService {
    constructor(musicbrainzDatabaseService, discogsArtistEnricherService) {
        this.musicbrainzDatabaseService = musicbrainzDatabaseService;
        this.enrichers = [discogsArtistEnricherService];
    }
    async enrich(mbId) {
        const mbArtist = await this.musicbrainzDatabaseService.getArtist(mbId);
        ArtistEnrichmentService_1.logger.debug(`Found artist '${mbArtist.name}' for Musicbrainz ID ${mbId}.`);
        const proposedEdits = [];
        for (const enricher of this.enrichers) {
            if (enricher.canEnrich(mbArtist)) {
                ArtistEnrichmentService_1.logger.debug(`Enricher ${enricher.name} can enrich '${mbArtist.name}'.`);
                const enricherEdits = await enricher.enrich(mbArtist);
                proposedEdits.push(...enricherEdits);
            }
            else {
                ArtistEnrichmentService_1.logger.debug(`Enricher ${enricher.name} cannot enrich '${mbArtist.name}', skipping.`);
            }
        }
        return proposedEdits;
    }
};
ArtistEnrichmentService.logger = logger_1.rootLogger.child({
    target: ArtistEnrichmentService_1
});
ArtistEnrichmentService = ArtistEnrichmentService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [MusicbrainzDatabaseService_1.MusicbrainzDatabaseService, DiscogsArtistEnricher_js_1.DiscogsArtistEnricher]
    }),
    __metadata("design:paramtypes", [MusicbrainzDatabaseService_1.MusicbrainzDatabaseService,
        DiscogsArtistEnricher_js_1.DiscogsArtistEnricher])
], ArtistEnrichmentService);
exports.ArtistEnrichmentService = ArtistEnrichmentService;
