import { IArtist } from "musicbrainz-api";
import { DiscogsDatabaseService } from "../../../api/discogs/DiscogsDatabaseService";
import { ArtistEnricher, ProposedArtistEdit } from "./ArtistEnricher.js";
declare class DiscogsArtistEnricher implements ArtistEnricher {
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
export { DiscogsArtistEnricher };
