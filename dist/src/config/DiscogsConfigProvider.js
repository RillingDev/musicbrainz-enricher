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
let DiscogsConfigProvider = class DiscogsConfigProvider extends AbstractCachingAsyncProvider_js_1.AbstractCachingAsyncProvider {
    async createInstance() {
        const consumerKey = process.env.DISCOGS_KEY;
        const consumerSecret = process.env.DISCOGS_SECRET;
        if (consumerKey == null) {
            throw new TypeError("Environment variable 'DISCOGS_KEY' is not set!");
        }
        if (consumerSecret == null) {
            throw new TypeError("Environment variable 'DISCOGS_SECRET' is not set!");
        }
        const { version, name } = await readPkg();
        return {
            userAgent: `${name}/${version}`,
            auth: {
                consumerKey,
                consumerSecret
            }
        };
    }
};
DiscogsConfigProvider = __decorate([
    chevronjs_1.Injectable(chevron_js_1.chevron)
], DiscogsConfigProvider);
exports.DiscogsConfigProvider = DiscogsConfigProvider;
