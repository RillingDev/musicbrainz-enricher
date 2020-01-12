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
        const username = process.env.MUSICBRAINZ_USERNAME;
        const password = process.env.MUSICBRAINZ_PASSWORD;
        if (username == null) {
            throw new TypeError(
                "Environment variable 'MUSICBRAINZ_USERNAME' is not set!"
            );
        }
        if (password == null) {
            throw new TypeError(
                "Environment variable 'MUSICBRAINZ_PASSWORD' is not set!"
            );
        }
        const { version, name, author } = await readPkg();
        return {
            botAccount: {
                username,
                password
            },

            baseUrl: "https://musicbrainz.org/",

            appName: name,
            appVersion: version,
            appContactInfo: author?.email
        };
    }
}

export { MusicbrainzConfigProvider };
