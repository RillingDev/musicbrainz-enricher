import { Injectable } from "chevronjs";
import * as readPkg from "read-pkg";
import { chevron } from "../chevron.js";
import { AbstractCachingAsyncProvider } from "./AbstractCachingAsyncProvider.js";

interface DiscogsConfig {
    userAgent: string;
    auth: {
        consumerKey: string;
        consumerSecret: string;
    };
}

@Injectable(chevron)
class DiscogsConfigProvider extends AbstractCachingAsyncProvider<
    DiscogsConfig
> {
    protected async createInstance(): Promise<DiscogsConfig> {
        const consumerKey = process.env.DISCOGS_KEY;
        const consumerSecret = process.env.DISCOGS_SECRET;
        if (consumerKey == null) {
            throw new TypeError(
                "Environment variable 'DISCOGS_KEY' is not set!"
            );
        }
        if (consumerSecret == null) {
            throw new TypeError(
                "Environment variable 'DISCOGS_SECRET' is not set!"
            );
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
}

export { DiscogsConfig, DiscogsConfigProvider };
