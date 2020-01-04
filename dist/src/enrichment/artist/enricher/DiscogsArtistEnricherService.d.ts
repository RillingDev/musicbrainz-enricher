import { IArtist } from "musicbrainz-api";
import { ArtistEnricherService, ProposedArtistEdit } from "./ArtistEnricherService";
import { DiscogsDatabaseService } from "../../../api/discogs/DiscogsDatabaseService";
declare class DiscogsArtistEnricherService implements ArtistEnricherService {
    private readonly discogsDatabaseService;
    private static readonly logger;
    private static readonly DISCOGS_URL_ID_PATTERN;
    readonly name = "Discogs";
    constructor(discogsDatabaseService: DiscogsDatabaseService);
    canEnrich(mbArtist: IArtist): boolean;
    enrich(mbArtist: IArtist): Promise<ProposedArtistEdit[]>;
    private enrichLegalNameFromDiscogs;
    private getDiscogsId;
}
export { DiscogsArtistEnricherService };
