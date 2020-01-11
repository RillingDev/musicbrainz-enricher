import { Injectable } from "chevronjs";
import { MusicbrainzDatabaseService } from "../../api/musicbrainz/MusicbrainzDatabaseService";
import { chevron } from "../../chevron";
import { rootLogger } from "../../logger";
import {
    ArtistEnricher,
    ProposedArtistEdit
} from "./enricher/ArtistEnricher.js";
import { DiscogsArtistEnricher } from "./enricher/DiscogsArtistEnricher.js";

@Injectable(chevron, {
    dependencies: [MusicbrainzDatabaseService, DiscogsArtistEnricher]
})
class ArtistEnrichmentService {
    private static readonly logger = rootLogger.child({
        target: ArtistEnrichmentService
    });
    private readonly enrichers: ArtistEnricher[];

    constructor(
        private readonly musicbrainzDatabaseService: MusicbrainzDatabaseService,
        discogsArtistEnricherService: DiscogsArtistEnricher
    ) {
        this.enrichers = [discogsArtistEnricherService];
    }

    public async enrich(mbId: string): Promise<ProposedArtistEdit[]> {
        const mbArtist = await this.musicbrainzDatabaseService.getArtist(mbId);
        ArtistEnrichmentService.logger.debug(
            `Found artist '${mbArtist.name}' for Musicbrainz ID ${mbId}.`
        );

        const proposedEdits = [];
        for (const enricher of this.enrichers) {
            if (enricher.canEnrich(mbArtist)) {
                ArtistEnrichmentService.logger.debug(
                    `Enricher ${enricher.name} can enrich '${mbArtist.name}'.`
                );
                const enricherEdits = await enricher.enrich(mbArtist);
                proposedEdits.push(...enricherEdits);
            } else {
                ArtistEnrichmentService.logger.debug(
                    `Enricher ${enricher.name} cannot enrich '${mbArtist.name}', skipping.`
                );
            }
        }

        return proposedEdits;
    }
}

export { ArtistEnrichmentService };
