import { Injectable } from "chevronjs";
import { isEmpty } from "lodash";
import { IArtist } from "musicbrainz-api";
import { DiscogsDatabaseService } from "../../../api/discogs/DiscogsDatabaseService";
import { DiscogsArtist } from "../../../api/discogs/schema/DiscogsArtist";
import { chevron } from "../../../chevron";
import { EditType } from "../../../edit/ProposedEdit.js";
import { rootLogger } from "../../../logger";
import { ArtistEnricher, ProposedArtistEdit } from "./ArtistEnricher.js";

@Injectable(chevron, { dependencies: [DiscogsDatabaseService] })
class DiscogsArtistEnricher implements ArtistEnricher {
    private static readonly logger = rootLogger.child({
        target: DiscogsArtistEnricher
    });
    private static readonly DISCOGS_URL_ID_PATTERN = /\/(\d+)$/;

    public readonly name = "Discogs";

    constructor(
        private readonly discogsDatabaseService: DiscogsDatabaseService
    ) {}

    public canEnrich(mbArtist: IArtist): boolean {
        return this.getDiscogsId(mbArtist) != null;
    }

    public async enrich(mbArtist: IArtist): Promise<ProposedArtistEdit[]> {
        if (!this.canEnrich(mbArtist)) {
            throw new TypeError("Cannot enrich this artist!");
        }

        const discogsId = this.getDiscogsId(mbArtist)!;
        const discogsArtist = await this.discogsDatabaseService.getArtist(
            discogsId
        );
        if (discogsArtist == null) {
            DiscogsArtistEnricher.logger.warn(
                `Could not find discogs artist by ID.`
            );
            return [];
        }
        DiscogsArtistEnricher.logger.silly(`Found discogs artist by ID.`);

        return [...this.enrichLegalNameFromDiscogs(mbArtist, discogsArtist)];
    }

    private enrichLegalNameFromDiscogs(
        mbArtist: IArtist,
        discogsArtist: DiscogsArtist
    ): ProposedArtistEdit[] {
        const mbAliases = mbArtist.aliases ?? [];
        const mbLegalNames = mbAliases.filter(
            alias => alias.type === "Legal name"
        );
        const discogsLegalName = discogsArtist.realname;

        // No Discogs legal name was found.
        if (discogsLegalName == null) {
            DiscogsArtistEnricher.logger.info(`No legal name found.`);
            return [];
        }

        // Discogs legal name was found, but is same as Musicbrainz name.
        if (discogsLegalName === mbArtist.name) {
            DiscogsArtistEnricher.logger.info(
                `Legal name '${discogsLegalName}' is already used as main name in Musicbrainz.`
            );
            return [];
        }

        // Discogs legal name was found, no Musicbrainz legal name names exist
        // yet.
        if (isEmpty(mbLegalNames)) {
            DiscogsArtistEnricher.logger.info(
                `Found new legal name '${discogsLegalName}'.`
            );
            return [
                {
                    type: EditType.ADD,
                    target: mbArtist,
                    property: "legal name",
                    new: discogsLegalName
                }
            ];
        }

        const differentLegalNames = mbLegalNames.filter(
            alias => alias.name !== discogsLegalName
        );
        // Discogs legal name was found, but Musicbrainz legal name also
        // exist with same value
        if (isEmpty(differentLegalNames)) {
            DiscogsArtistEnricher.logger.info(
                `Found legal name '${discogsLegalName}' that already exist as legal name in Musicbrainz.`
            );
            return [];
        }

        // Discogs legal name was found, but Musicbrainz legal name(s) also
        // exist with different values
        DiscogsArtistEnricher.logger.info(
            `Found legal name '${discogsLegalName}' that is different from existing Musicbrainz` +
                ` legal name(s) '${differentLegalNames.map(
                    alias => alias.name
                )}'.`
        );
        return [
            {
                type: EditType.CHANGE,
                target: mbArtist,
                property: "legal name",
                old: mbLegalNames.map(alias => alias.name),
                new: discogsLegalName
            }
        ];
    }

    private getDiscogsId(artist: IArtist): string | null {
        if (artist.relations == null) {
            return null;
        }

        const discogsRelation = artist.relations.find(
            rel => rel.type === "discogs"
        );
        if (discogsRelation?.url == null) {
            return null;
        }

        const exec = DiscogsArtistEnricher.DISCOGS_URL_ID_PATTERN.exec(
            discogsRelation.url.resource
        );
        if (exec == null) {
            return null;
        }

        return exec[1];
    }
}

export { DiscogsArtistEnricher };
