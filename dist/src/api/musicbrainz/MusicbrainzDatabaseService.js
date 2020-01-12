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
const MusicbrainzConfigProvider_js_1 = require("../../config/MusicbrainzConfigProvider.js");
const logger_1 = require("../../logger");
const AsyncService_js_1 = require("../AsyncService.js");
let MusicbrainzDatabaseService = MusicbrainzDatabaseService_1 = class MusicbrainzDatabaseService {
    constructor(musicbrainzConfigProvider, asyncService) {
        this.musicbrainzConfigProvider = musicbrainzConfigProvider;
        this.asyncService = asyncService;
    }
    async getArtist(mbId) {
        const client = await this.createClient();
        return client.getArtist(mbId, ["aliases", "url-rels"]);
    }
    async searchArtist(formData, consumer) {
        const client = await this.createClient();
        await this.fetchAll(offset => client.searchArtist(formData, offset), result => result.artists, consumer);
    }
    async fetchAll(searchProvider, itemListExtractor, consumer) {
        let offset = 0;
        let totalCount = null;
        do {
            const response = await searchProvider(offset);
            if (totalCount == null) {
                totalCount = response.count;
            }
            const itemList = itemListExtractor(response);
            offset += itemList.length;
            await this.asyncService.queue(itemList.map(item => () => consumer(item)));
        } while (totalCount != null && offset < totalCount);
    }
    async createClient() {
        return new musicbrainz_api_1.MusicBrainzApi(await this.musicbrainzConfigProvider.getInstance());
    }
};
MusicbrainzDatabaseService.logger = logger_1.rootLogger.child({
    target: MusicbrainzDatabaseService_1
});
MusicbrainzDatabaseService = MusicbrainzDatabaseService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [MusicbrainzConfigProvider_js_1.MusicbrainzConfigProvider, AsyncService_js_1.AsyncService]
    }),
    __metadata("design:paramtypes", [MusicbrainzConfigProvider_js_1.MusicbrainzConfigProvider,
        AsyncService_js_1.AsyncService])
], MusicbrainzDatabaseService);
exports.MusicbrainzDatabaseService = MusicbrainzDatabaseService;
