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
const DiscogsDatabaseService_1 = require("../api/discogs/DiscogsDatabaseService");
const MusicbrainzDatabaseService_1 = require("../api/musicbrainz/MusicbrainzDatabaseService");
const MusicbrainzService_1 = require("../api/musicbrainz/MusicbrainzService");
const chevron_1 = require("../chevron");
const logger_1 = require("../logger");
let ArtistEnrichmentService = ArtistEnrichmentService_1 = class ArtistEnrichmentService {
    constructor(musicbrainzDatabaseService, discogsDatabaseService, musicbrainzService) {
        this.musicbrainzDatabaseService = musicbrainzDatabaseService;
        this.discogsDatabaseService = discogsDatabaseService;
        this.musicbrainzService = musicbrainzService;
    }
    async enrich(mbId) {
        const artist = await this.musicbrainzDatabaseService.getArtist(mbId);
        ArtistEnrichmentService_1.logger.debug(`Found artist '${artist.name}'.`);
        const discogsId = this.musicbrainzService.getDiscogsId(artist);
        if (discogsId != null) {
            ArtistEnrichmentService_1.logger.debug(`Found discogs ID ${discogsId} for artist '${artist.name}'.`);
            await this.enrichFromDiscogs(artist, discogsId);
        }
        else {
            ArtistEnrichmentService_1.logger.debug(`Could not find '${artist.name}' discogs ID.`);
        }
    }
    async enrichFromDiscogs(artist, discogsId) {
        var _a, _b;
        const aliases = (_a = artist.aliases, (_a !== null && _a !== void 0 ? _a : []));
        const relations = (_b = artist.relations, (_b !== null && _b !== void 0 ? _b : []));
        const discogsArtist = await this.discogsDatabaseService.getArtist(discogsId);
        ArtistEnrichmentService_1.logger.debug(`Found discogs artist by id.`);
        console.log(discogsArtist);
    }
};
ArtistEnrichmentService.logger = logger_1.rootLogger.child({
    target: ArtistEnrichmentService_1
});
ArtistEnrichmentService = ArtistEnrichmentService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [
            MusicbrainzDatabaseService_1.MusicbrainzDatabaseService,
            DiscogsDatabaseService_1.DiscogsDatabaseService,
            MusicbrainzService_1.MusicbrainzService
        ]
    }),
    __metadata("design:paramtypes", [MusicbrainzDatabaseService_1.MusicbrainzDatabaseService,
        DiscogsDatabaseService_1.DiscogsDatabaseService,
        MusicbrainzService_1.MusicbrainzService])
], ArtistEnrichmentService);
exports.ArtistEnrichmentService = ArtistEnrichmentService;
