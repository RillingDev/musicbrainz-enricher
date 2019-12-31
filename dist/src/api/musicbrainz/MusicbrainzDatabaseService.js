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
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const musicbrainz_api_1 = require("musicbrainz-api");
const chevron_1 = require("../../chevron");
let MusicbrainzDatabaseService = class MusicbrainzDatabaseService {
    constructor(musicbrainzConfig) {
        this.client = new musicbrainz_api_1.MusicBrainzApi(musicbrainzConfig);
    }
    getArtist(mbId) {
        return this.client.getArtist(mbId, ["aliases", "url-rels"]);
    }
    search(formData) {
        return this.client.searchArtist(formData);
    }
};
MusicbrainzDatabaseService = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, { dependencies: ["musicbrainzConfig"] }),
    __metadata("design:paramtypes", [Object])
], MusicbrainzDatabaseService);
exports.MusicbrainzDatabaseService = MusicbrainzDatabaseService;
