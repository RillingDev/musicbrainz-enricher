import { DiscogsDatabaseService } from "../api/discogs/DiscogsDatabaseService";
import { MusicbrainzDatabaseService } from "../api/musicbrainz/MusicbrainzDatabaseService";
import { MusicbrainzService } from "../util/MusicbrainzService";
declare class ArtistEnrichmentService {
    private readonly musicbrainzDatabaseService;
    private readonly discogsDatabaseService;
    private readonly musicbrainzService;
    private static readonly logger;
    constructor(musicbrainzDatabaseService: MusicbrainzDatabaseService, discogsDatabaseService: DiscogsDatabaseService, musicbrainzService: MusicbrainzService);
    enrich(mbId: string): Promise<void>;
    private enrichFromDiscogs;
    private enrichLegalNameFromDiscogs;
}
export { ArtistEnrichmentService };
