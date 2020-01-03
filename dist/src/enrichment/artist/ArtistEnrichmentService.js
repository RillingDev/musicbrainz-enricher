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
const DiscogsDatabaseService_1 = require("../../api/discogs/DiscogsDatabaseService");
const MusicbrainzDatabaseService_1 = require("../../api/musicbrainz/MusicbrainzDatabaseService");
const chevron_1 = require("../../chevron");
const logger_1 = require("../../logger");
const RelationshipService_1 = require("./RelationshipService");
let ArtistEnrichmentService = ArtistEnrichmentService_1 = class ArtistEnrichmentService {
    constructor(musicbrainzDatabaseService, discogsDatabaseService, relationshipService) {
        this.musicbrainzDatabaseService = musicbrainzDatabaseService;
        this.discogsDatabaseService = discogsDatabaseService;
        this.relationshipService = relationshipService;
    }
    async enrich(mbId) {
        const mbArtist = await this.musicbrainzDatabaseService.getArtist(mbId);
        ArtistEnrichmentService_1.logger.debug(`Found artist '${mbArtist.name}' for Musicbrainz ID ${mbId}.`);
        const discogsId = this.relationshipService.getDiscogsId(mbArtist);
        if (discogsId != null) {
            ArtistEnrichmentService_1.logger.debug(`Found discogs ID ${discogsId} for artist '${mbArtist.name}'.`);
            await this.enrichFromDiscogs(mbArtist, discogsId);
        }
        else {
            ArtistEnrichmentService_1.logger.debug(`Could not find '${mbArtist.name}' discogs ID.`);
        }
    }
    async enrichFromDiscogs(mbArtist, discogsId) {
        const discogsArtist = await this.discogsDatabaseService.getArtist(discogsId);
        if (discogsArtist == null) {
            ArtistEnrichmentService_1.logger.debug(`Could not find discogs artist by id.`);
            return;
        }
        ArtistEnrichmentService_1.logger.silly(`Found discogs artist by id.`);
        this.enrichLegalNameFromDiscogs(mbArtist, discogsArtist);
    }
    enrichLegalNameFromDiscogs(mbArtist, discogsArtist) {
        var _a;
        const aliases = (_a = mbArtist.aliases, (_a !== null && _a !== void 0 ? _a : []));
        const mbLegalNames = aliases.filter(alias => alias.type === "Legal name");
        const discogsLegalName = discogsArtist.realname;
        if (discogsLegalName == null) {
            ArtistEnrichmentService_1.logger.debug(`No Discogs name found for'${mbArtist.name}'.`);
            return;
        }
        if (discogsLegalName === mbArtist.name) {
            ArtistEnrichmentService_1.logger.debug(`Legal name ${discogsLegalName} is already used as main name '${mbArtist.name}'.`);
        }
        else if (mbLegalNames.length === 0) {
            ArtistEnrichmentService_1.logger.info(`Found new legal name ${discogsLegalName} for Musicbrainz artist '${mbArtist.name}'.`);
        }
        else {
            const differentLegalNames = mbLegalNames.filter(alias => alias.name !== discogsLegalName);
            if (differentLegalNames.length > 0) {
                ArtistEnrichmentService_1.logger.info(`Found legal name '${discogsLegalName}' that is different from existing '${differentLegalNames.map(alias => alias.name)}'.`);
            }
        }
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
            RelationshipService_1.RelationshipService
        ]
    }),
    __metadata("design:paramtypes", [MusicbrainzDatabaseService_1.MusicbrainzDatabaseService,
        DiscogsDatabaseService_1.DiscogsDatabaseService,
        RelationshipService_1.RelationshipService])
], ArtistEnrichmentService);
exports.ArtistEnrichmentService = ArtistEnrichmentService;
