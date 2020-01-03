import { DiscogsDatabaseService } from "../../api/discogs/DiscogsDatabaseService";
import { MusicbrainzDatabaseService } from "../../api/musicbrainz/MusicbrainzDatabaseService";
import { RelationshipService } from "./RelationshipService";
declare class ArtistEnrichmentService {
    private readonly musicbrainzDatabaseService;
    private readonly discogsDatabaseService;
    private readonly relationshipService;
    private static readonly logger;
    constructor(musicbrainzDatabaseService: MusicbrainzDatabaseService, discogsDatabaseService: DiscogsDatabaseService, relationshipService: RelationshipService);
    enrich(mbId: string): Promise<void>;
    private enrichFromDiscogs;
    private enrichLegalNameFromDiscogs;
}
export { ArtistEnrichmentService };
