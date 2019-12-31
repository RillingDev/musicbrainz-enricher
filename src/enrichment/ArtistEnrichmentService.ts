import { Injectable } from "chevronjs";
import { IArtist } from "musicbrainz-api";
import { DiscogsDatabaseService } from "../api/discogs/DiscogsDatabaseService";
import { DiscogsArtist } from "../api/discogs/schema/DiscogsArtist";
import { MusicbrainzDatabaseService } from "../api/musicbrainz/MusicbrainzDatabaseService";
import { MusicbrainzService } from "../api/musicbrainz/MusicbrainzService";
import { chevron } from "../chevron";
import { rootLogger } from "../logger";

@Injectable(chevron, {
    dependencies: [
        MusicbrainzDatabaseService,
        DiscogsDatabaseService,
        MusicbrainzService
    ]
})
class ArtistEnrichmentService {
    private static readonly logger = rootLogger.child({
        target: ArtistEnrichmentService
    });

    constructor(
        private readonly musicbrainzDatabaseService: MusicbrainzDatabaseService,
        private readonly discogsDatabaseService: DiscogsDatabaseService,
        private readonly musicbrainzService: MusicbrainzService
    ) {}

    public async enrich(mbId: string): Promise<void> {
        const mbArtist = await this.musicbrainzDatabaseService.getArtist(mbId);
        ArtistEnrichmentService.logger.debug(
            `Found artist '${mbArtist.name}'.`
        );

        const discogsId = this.musicbrainzService.getDiscogsId(mbArtist);
        if (discogsId != null) {
            ArtistEnrichmentService.logger.debug(
                `Found discogs ID ${discogsId} for artist '${mbArtist.name}'.`
            );
            await this.enrichFromDiscogs(mbArtist, discogsId);
        } else {
            ArtistEnrichmentService.logger.debug(
                `Could not find '${mbArtist.name}' discogs ID.`
            );
        }
    }

    private async enrichFromDiscogs(
        mbArtist: IArtist,
        discogsId: string
    ): Promise<void> {
        const discogsArtist = await this.discogsDatabaseService.getArtist(
            discogsId
        );
        if (discogsArtist == null) {
            ArtistEnrichmentService.logger.debug(
                `Could not find discogs artist by id.`
            );
            return;
        }
        ArtistEnrichmentService.logger.debug(`Found discogs artist by id.`);

        this.enrichLegalNameFromDiscogs(mbArtist, discogsArtist);
    }

    private enrichLegalNameFromDiscogs(
        mbArtist: IArtist,
        discogsArtist: DiscogsArtist
    ): void {
        const aliases = mbArtist.aliases ?? [];
        const mbLegalNames = aliases.filter(
            alias => alias.type === "Legal name"
        );
        const discogsLegalName = discogsArtist.realname;

        if (discogsLegalName != null) {
            if (mbLegalNames.length === 0) {
                ArtistEnrichmentService.logger.info(
                    `Found new legal name ${discogsLegalName}.`
                );
            } else {
                const differentLegalNames = mbLegalNames.filter(
                    alias => alias.name !== discogsLegalName
                );
                if (differentLegalNames.length > 0) {
                    ArtistEnrichmentService.logger.info(
                        `Found legal name '${discogsLegalName}' that is different from existing '${differentLegalNames.map(
                            alias => alias.name
                        )}'.`
                    );
                }
            }
        }
    }
}

export { ArtistEnrichmentService };
