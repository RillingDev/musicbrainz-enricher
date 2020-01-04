import { MusicbrainzDatabaseService } from "../../api/musicbrainz/MusicbrainzDatabaseService";
import { ProposedArtistEdit } from "./enricher/ArtistEnricherService";
import { DiscogsArtistEnricherService } from "./enricher/DiscogsArtistEnricherService";
declare class ArtistEnrichmentService {
    private readonly musicbrainzDatabaseService;
    private static readonly logger;
    private readonly enrichers;
    constructor(musicbrainzDatabaseService: MusicbrainzDatabaseService, discogsArtistEnricherService: DiscogsArtistEnricherService);
    enrich(mbId: string): Promise<ProposedArtistEdit[]>;
}
export { ArtistEnrichmentService };
