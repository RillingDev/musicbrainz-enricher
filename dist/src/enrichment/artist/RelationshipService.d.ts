import { IArtist } from "musicbrainz-api";
declare class RelationshipService {
    private static readonly DISCOGS_URL_ID_PATTERN;
    private static readonly DISCOGS_TYPE;
    getDiscogsId(artist: IArtist): string | null;
}
export { RelationshipService };
