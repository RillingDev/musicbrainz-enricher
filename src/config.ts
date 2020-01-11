import * as readPkg from "read-pkg";
import { chevron } from "./chevron.js";

const musicbrainzConfigInjectableName = "musicbrainzConfig";
const discogsConfigInjectableName = "discogsConfig";

interface DiscogsConfig {
    userAgent: string;
    auth: {
        consumerKey: string;
        consumerSecret: string;
    };
}

const initConfig = async (): Promise<void> => {
    const { version, name, author } = await readPkg();

    chevron.registerInjectable(
        {
            botAccount: {
                username: "unmasked_bot",
                password: process.env.MUSICBRAINZ_PASSWORD
            },

            baseUrl: "https://musicbrainz.org/",

            appName: name,
            appVersion: version,
            appContactInfo: author?.email
        },
        { name: musicbrainzConfigInjectableName }
    );

    chevron.registerInjectable(
        {
            userAgent: `${name}/${version}`,
            auth: {
                consumerKey: process.env.DISCOGS_KEY,
                consumerSecret: process.env.DISCOGS_SECRET
            }
        },
        { name: discogsConfigInjectableName }
    );
};

export {
    initConfig,
    musicbrainzConfigInjectableName,
    discogsConfigInjectableName,
    DiscogsConfig
};
