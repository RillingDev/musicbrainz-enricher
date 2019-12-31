import { IArtist } from "musicbrainz-api";
declare class MusicbrainzService {
    private static readonly DISCOCGS_URL_ID_PATTERN;
    private static readonly DISCOGS_TYPE;
    getDiscogsId(artist: IArtist): string | null;
}
export { MusicbrainzService };
