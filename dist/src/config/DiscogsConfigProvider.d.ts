import { AbstractCachingAsyncProvider } from "./AbstractCachingAsyncProvider.js";
interface DiscogsConfig {
    userAgent: string;
    auth: {
        consumerKey: string;
        consumerSecret: string;
    };
}
declare class DiscogsConfigProvider extends AbstractCachingAsyncProvider<DiscogsConfig> {
    protected createInstance(): Promise<DiscogsConfig>;
}
export { DiscogsConfig, DiscogsConfigProvider };
