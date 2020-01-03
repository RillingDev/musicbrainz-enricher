import { Injectable } from "chevronjs";
import { IArtist } from "musicbrainz-api";
import { chevron } from "../../chevron";

@Injectable(chevron)
class RelationshipService {
    private static readonly DISCOGS_URL_ID_PATTERN = /\/(\d+)$/;

    private static readonly DISCOGS_TYPE = "discogs";

    public getDiscogsId(artist: IArtist): string | null {
        if (artist.relations == null) {
            return null;
        }

        const discogsRelation = artist.relations.find(
            rel => rel.type === RelationshipService.DISCOGS_TYPE
        );
        if (discogsRelation?.url == null) {
            return null;
        }

        const exec = RelationshipService.DISCOGS_URL_ID_PATTERN.exec(
            discogsRelation.url.resource
        );
        if (exec == null) {
            return null;
        }

        return exec[1];
    }
}

export { RelationshipService };
