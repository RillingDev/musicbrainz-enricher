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
var MusicbrainzDatabaseService_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const musicbrainz_api_1 = require("musicbrainz-api");
const chevron_1 = require("../../chevron");
const config_js_1 = require("../../config.js");
const logger_1 = require("../../logger");
const AsyncService_1 = require("../../util/AsyncService");
let MusicbrainzDatabaseService = MusicbrainzDatabaseService_1 = class MusicbrainzDatabaseService {
    constructor(musicbrainzConfig, asyncService) {
        this.asyncService = asyncService;
        this.client = new musicbrainz_api_1.MusicBrainzApi(musicbrainzConfig);
    }
    async getArtist(mbId) {
        return this.client.getArtist(mbId, ["aliases", "url-rels"]);
    }
    async searchArtist(formData, consumer) {
        let offset = 0;
        let totalCount;
        do {
            MusicbrainzDatabaseService_1.logger.debug(`Searching artist with form data '${JSON.stringify(formData)}' and offset ${offset}.`);
            const response = await this.client.searchArtist(formData, offset);
            totalCount = response.count;
            offset += response.artists.length;
            await this.asyncService.queue(response.artists.map(artist => () => consumer(artist)));
        } while (offset < totalCount);
    }
};
MusicbrainzDatabaseService.logger = logger_1.rootLogger.child({
    target: MusicbrainzDatabaseService_1
});
MusicbrainzDatabaseService = MusicbrainzDatabaseService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [config_js_1.musicbrainzConfigInjectableName, AsyncService_1.AsyncService]
    }),
    __metadata("design:paramtypes", [Object, AsyncService_1.AsyncService])
], MusicbrainzDatabaseService);
exports.MusicbrainzDatabaseService = MusicbrainzDatabaseService;
