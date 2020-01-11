"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const readPkg = require("read-pkg");
const chevron_js_1 = require("./chevron.js");
const musicbrainzConfigInjectableName = "musicbrainzConfig";
exports.musicbrainzConfigInjectableName = musicbrainzConfigInjectableName;
const discogsConfigInjectableName = "discogsConfig";
exports.discogsConfigInjectableName = discogsConfigInjectableName;
const initConfig = async () => {
    var _a;
    const { version, name, author } = await readPkg();
    chevron_js_1.chevron.registerInjectable({
        botAccount: {
            username: "unmasked_bot",
            password: process.env.MUSICBRAINZ_PASSWORD
        },
        baseUrl: "https://musicbrainz.org/",
        appName: name,
        appVersion: version,
        appContactInfo: (_a = author) === null || _a === void 0 ? void 0 : _a.email
    }, { name: musicbrainzConfigInjectableName });
    chevron_js_1.chevron.registerInjectable({
        userAgent: `${name}/${version}`,
        auth: {
            consumerKey: process.env.DISCOGS_KEY,
            consumerSecret: process.env.DISCOGS_SECRET
        }
    }, { name: discogsConfigInjectableName });
};
exports.initConfig = initConfig;
