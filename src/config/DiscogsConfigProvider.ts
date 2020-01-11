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
        const { version, name } = await readPkg();
        return {
            userAgent: `${name}/${version}`,
            auth: {
                consumerKey: process.env.DISCOGS_KEY!,
                consumerSecret: process.env.DISCOGS_SECRET!
            }
        };
    }
}

export { DiscogsConfig, DiscogsConfigProvider };
