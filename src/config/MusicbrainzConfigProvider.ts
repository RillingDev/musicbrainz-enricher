import { Injectable } from "chevronjs";
import { IMusicBrainzConfig } from "musicbrainz-api";
import * as readPkg from "read-pkg";
import { chevron } from "../chevron.js";
import { AbstractCachingAsyncProvider } from "./AbstractCachingAsyncProvider.js";

@Injectable(chevron)
class MusicbrainzConfigProvider extends AbstractCachingAsyncProvider<
    IMusicBrainzConfig
> {
    protected async createInstance(): Promise<IMusicBrainzConfig> {
        const { version, name, author } = await readPkg();
        return {
            botAccount: {
                username: process.env.MUSICBRAINZ_USERNAME!,
                password: process.env.MUSICBRAINZ_PASSWORD!
            },

            baseUrl: "https://musicbrainz.org/",

            appName: name,
            appVersion: version,
            appContactInfo: author?.email
        };
    }
}

export { MusicbrainzConfigProvider };
