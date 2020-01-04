import { IArtist } from "musicbrainz-api";
import { ProposedEdit } from "../../ProposedEdit";

type ProposedArtistEdit = ProposedEdit<IArtist, string | string[]>;

interface ArtistEnricherService {
    readonly name: string;

    canEnrich(mbArtist: IArtist): boolean;

    enrich(mbArtist: IArtist): Promise<ProposedArtistEdit[]>;
}

export { ArtistEnricherService, ProposedArtistEdit };
