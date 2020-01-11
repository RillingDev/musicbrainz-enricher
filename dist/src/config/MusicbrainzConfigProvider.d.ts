import { IMusicBrainzConfig } from "musicbrainz-api";
import { AbstractCachingAsyncProvider } from "./AbstractCachingAsyncProvider.js";
declare class MusicbrainzConfigProvider extends AbstractCachingAsyncProvider<IMusicBrainzConfig> {
    protected createInstance(): Promise<IMusicBrainzConfig>;
}
export { MusicbrainzConfigProvider };
