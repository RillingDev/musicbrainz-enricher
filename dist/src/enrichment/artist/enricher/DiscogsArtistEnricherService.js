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
var DiscogsArtistEnricherService_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const DiscogsDatabaseService_1 = require("../../../api/discogs/DiscogsDatabaseService");
const chevron_1 = require("../../../chevron");
const logger_1 = require("../../../logger");
const ProposedEdit_1 = require("../../ProposedEdit");
let DiscogsArtistEnricherService = DiscogsArtistEnricherService_1 = class DiscogsArtistEnricherService {
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
            DiscogsArtistEnricherService_1.logger.debug(`Could not find discogs artist by id.`);
            return [];
        }
        DiscogsArtistEnricherService_1.logger.silly(`Found discogs artist by id.`);
        const proposedEdits = [];
        const proposedLegalNameEdit = this.enrichLegalNameFromDiscogs(mbArtist, discogsArtist);
        if (proposedLegalNameEdit != null) {
            proposedEdits.push(proposedLegalNameEdit);
        }
        return proposedEdits;
    }
    enrichLegalNameFromDiscogs(mbArtist, discogsArtist) {
        var _a;
        const aliases = (_a = mbArtist.aliases, (_a !== null && _a !== void 0 ? _a : []));
        const mbLegalNames = aliases.filter(alias => alias.type === "Legal name");
        const discogsLegalName = discogsArtist.realname;
        if (discogsLegalName == null) {
            DiscogsArtistEnricherService_1.logger.debug(`No Discogs name found for'${mbArtist.name}'.`);
            return null;
        }
        if (discogsLegalName === mbArtist.name) {
            DiscogsArtistEnricherService_1.logger.debug(`Legal name ${discogsLegalName} is already used as main name '${mbArtist.name}'.`);
            return null;
        }
        else if (mbLegalNames.length === 0) {
            DiscogsArtistEnricherService_1.logger.debug(`Found new legal name ${discogsLegalName} for Musicbrainz artist '${mbArtist.name}'.`);
            return {
                type: ProposedEdit_1.EditType.ADD,
                target: mbArtist,
                property: "legal name",
                new: discogsLegalName
            };
        }
        const differentLegalNames = mbLegalNames.filter(alias => alias.name !== discogsLegalName);
        if (differentLegalNames.length > 0) {
            DiscogsArtistEnricherService_1.logger.debug(`Found legal name '${discogsLegalName}' that is different from existing '${differentLegalNames.map(alias => alias.name)}'.`);
            return {
                type: ProposedEdit_1.EditType.CHANGE,
                target: mbArtist,
                property: "legal name",
                old: mbLegalNames.map(alias => alias.name),
                new: discogsLegalName
            };
        }
        return null;
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
        const exec = DiscogsArtistEnricherService_1.DISCOGS_URL_ID_PATTERN.exec(discogsRelation.url.resource);
        if (exec == null) {
            return null;
        }
        return exec[1];
    }
};
DiscogsArtistEnricherService.logger = logger_1.rootLogger.child({
    target: DiscogsArtistEnricherService_1
});
DiscogsArtistEnricherService.DISCOGS_URL_ID_PATTERN = /\/(\d+)$/;
DiscogsArtistEnricherService = DiscogsArtistEnricherService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, { dependencies: [DiscogsDatabaseService_1.DiscogsDatabaseService] }),
    __metadata("design:paramtypes", [DiscogsDatabaseService_1.DiscogsDatabaseService])
], DiscogsArtistEnricherService);
exports.DiscogsArtistEnricherService = DiscogsArtistEnricherService;
