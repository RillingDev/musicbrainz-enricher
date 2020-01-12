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
var DiscogsArtistEnricher_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const lodash_1 = require("lodash");
const DiscogsDatabaseService_1 = require("../../../api/discogs/DiscogsDatabaseService");
const chevron_1 = require("../../../chevron");
const logger_1 = require("../../../logger");
let DiscogsArtistEnricher = DiscogsArtistEnricher_1 = class DiscogsArtistEnricher {
    constructor(discogsDatabaseService) {
        this.discogsDatabaseService = discogsDatabaseService;
        this.name = "Discogs";
    }
    canEnrich(mbArtist) {
        return this.getDiscogsId(mbArtist) != null;
    }
    async enrich(mbArtist) {
        if (!this.canEnrich(mbArtist)) {
            throw new TypeError("Cannot enrich this artist!");
        }
        const discogsId = this.getDiscogsId(mbArtist);
        const discogsArtist = await this.discogsDatabaseService.getArtist(discogsId);
        if (discogsArtist == null) {
            DiscogsArtistEnricher_1.logger.warn(`Could not find discogs artist by ID.`);
            return [];
        }
        DiscogsArtistEnricher_1.logger.silly(`Found discogs artist by ID.`);
        return [...this.enrichLegalNameFromDiscogs(mbArtist, discogsArtist)];
    }
    enrichLegalNameFromDiscogs(mbArtist, discogsArtist) {
        var _a;
        const mbAliases = (_a = mbArtist.aliases, (_a !== null && _a !== void 0 ? _a : []));
        const mbLegalNames = mbAliases.filter(alias => alias.type === "Legal name");
        const discogsLegalName = discogsArtist.realname;
        // No Discogs legal name was found.
        if (discogsLegalName == null) {
            DiscogsArtistEnricher_1.logger.info(`No legal name found.`);
            return [];
        }
        // Discogs legal name was found, but is same as Musicbrainz name.
        if (discogsLegalName === mbArtist.name) {
            DiscogsArtistEnricher_1.logger.info(`Legal name '${discogsLegalName}' is already used as main name in Musicbrainz.`);
            return [];
        }
        // Discogs legal name was found, no Musicbrainz legal name names exist
        // yet.
        if (lodash_1.isEmpty(mbLegalNames)) {
            DiscogsArtistEnricher_1.logger.info(`Found new legal name '${discogsLegalName}'.`);
            return [
                {
                    type: "ADD" /* ADD */,
                    target: mbArtist,
                    property: "legal name",
                    new: discogsLegalName
                }
            ];
        }
        const differentLegalNames = mbLegalNames.filter(alias => alias.name !== discogsLegalName);
        // Discogs legal name was found, but Musicbrainz legal name also
        // exist with same value
        if (lodash_1.isEmpty(differentLegalNames)) {
            DiscogsArtistEnricher_1.logger.info(`Found legal name '${discogsLegalName}' that already exist as legal name in Musicbrainz.`);
            return [];
        }
        // Discogs legal name was found, but Musicbrainz legal name(s) also
        // exist with different values
        DiscogsArtistEnricher_1.logger.info(`Found legal name '${discogsLegalName}' that is different from existing Musicbrainz` +
            ` legal name(s) '${differentLegalNames.map(alias => alias.name)}'.`);
        return [
            {
                type: "CHECK/CHANGE" /* CHANGE */,
                target: mbArtist,
                property: "legal name",
                old: mbLegalNames.map(alias => alias.name),
                new: discogsLegalName
            }
        ];
    }
    getDiscogsId(artist) {
        var _a;
        if (artist.relations == null) {
            return null;
        }
        const discogsRelation = artist.relations.find(rel => rel.type === "discogs");
        if (((_a = discogsRelation) === null || _a === void 0 ? void 0 : _a.url) == null) {
            return null;
        }
        const exec = DiscogsArtistEnricher_1.DISCOGS_URL_ID_PATTERN.exec(discogsRelation.url.resource);
        if (exec == null) {
            return null;
        }
        return exec[1];
    }
};
DiscogsArtistEnricher.logger = logger_1.rootLogger.child({
    target: DiscogsArtistEnricher_1
});
DiscogsArtistEnricher.DISCOGS_URL_ID_PATTERN = /\/(\d+)$/;
DiscogsArtistEnricher = DiscogsArtistEnricher_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, { dependencies: [DiscogsDatabaseService_1.DiscogsDatabaseService] }),
    __metadata("design:paramtypes", [DiscogsDatabaseService_1.DiscogsDatabaseService])
], DiscogsArtistEnricher);
exports.DiscogsArtistEnricher = DiscogsArtistEnricher;
