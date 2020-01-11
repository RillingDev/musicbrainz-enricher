declare const musicbrainzConfigInjectableName = "musicbrainzConfig";
declare const discogsConfigInjectableName = "discogsConfig";
interface DiscogsConfig {
    userAgent: string;
    auth: {
        consumerKey: string;
        consumerSecret: string;
    };
}
declare const initConfig: () => Promise<void>;
export { initConfig, musicbrainzConfigInjectableName, discogsConfigInjectableName, DiscogsConfig };
