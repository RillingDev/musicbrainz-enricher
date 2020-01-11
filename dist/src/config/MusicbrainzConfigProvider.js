"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const readPkg = require("read-pkg");
const chevron_js_1 = require("../chevron.js");
const AbstractCachingAsyncProvider_js_1 = require("./AbstractCachingAsyncProvider.js");
let MusicbrainzConfigProvider = class MusicbrainzConfigProvider extends AbstractCachingAsyncProvider_js_1.AbstractCachingAsyncProvider {
    async createInstance() {
        var _a;
        const { version, name, author } = await readPkg();
        return {
            botAccount: {
                username: process.env.MUSICBRAINZ_USERNAME,
                password: process.env.MUSICBRAINZ_PASSWORD
            },
            baseUrl: "https://musicbrainz.org/",
            appName: name,
            appVersion: version,
            appContactInfo: (_a = author) === null || _a === void 0 ? void 0 : _a.email
        };
    }
};
MusicbrainzConfigProvider = __decorate([
    chevronjs_1.Injectable(chevron_js_1.chevron)
], MusicbrainzConfigProvider);
exports.MusicbrainzConfigProvider = MusicbrainzConfigProvider;
