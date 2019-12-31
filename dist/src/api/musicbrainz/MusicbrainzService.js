"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var MusicbrainzService_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const chevron_1 = require("../../chevron");
let MusicbrainzService = MusicbrainzService_1 = class MusicbrainzService {
    getDiscogsId(artist) {
        var _a;
        if (artist.relations == null) {
            return null;
        }
        const discogsRelation = artist.relations.find(rel => rel.type === MusicbrainzService_1.DISCOGS_TYPE);
        if (((_a = discogsRelation) === null || _a === void 0 ? void 0 : _a.url) == null) {
            return null;
        }
        const exec = MusicbrainzService_1.DISCOCGS_URL_ID_PATTERN.exec(discogsRelation.url.resource);
        if (exec == null) {
            return null;
        }
        return exec[1];
    }
};
MusicbrainzService.DISCOCGS_URL_ID_PATTERN = /\/(\d+)$/;
MusicbrainzService.DISCOGS_TYPE = "discogs";
MusicbrainzService = MusicbrainzService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron)
], MusicbrainzService);
exports.MusicbrainzService = MusicbrainzService;
