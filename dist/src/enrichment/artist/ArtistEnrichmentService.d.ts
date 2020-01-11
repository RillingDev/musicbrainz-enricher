import { MusicbrainzDatabaseService } from "../../api/musicbrainz/MusicbrainzDatabaseService";
import { ProposedArtistEdit } from "./enricher/ArtistEnricher.js";
import { DiscogsArtistEnricher } from "./enricher/DiscogsArtistEnricher.js";
declare class ArtistEnrichmentService {
    private readonly musicbrainzDatabaseService;
    private static readonly logger;
    private readonly enrichers;
    constructor(musicbrainzDatabaseService: MusicbrainzDatabaseService, discogsArtistEnricherService: DiscogsArtistEnricher);
    enrich(mbId: string): Promise<ProposedArtistEdit[]>;
}
export { ArtistEnrichmentService };
