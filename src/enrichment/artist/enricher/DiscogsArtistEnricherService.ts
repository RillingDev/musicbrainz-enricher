import { Injectable } from "chevronjs";
import { IArtist } from "musicbrainz-api";
import { DiscogsDatabaseService } from "../../../api/discogs/DiscogsDatabaseService";
import { DiscogsArtist } from "../../../api/discogs/schema/DiscogsArtist";
import { chevron } from "../../../chevron";
import { EditType } from "../../../edit/ProposedEdit.js";
import { rootLogger } from "../../../logger";
import {
    ArtistEnricherService,
    ProposedArtistEdit
} from "./ArtistEnricherService";

@Injectable(chevron, { dependencies: [DiscogsDatabaseService] })
class DiscogsArtistEnricherService implements ArtistEnricherService {
    private static readonly logger = rootLogger.child({
        target: DiscogsArtistEnricherService
    });
    private static readonly DISCOGS_URL_ID_PATTERN = /\/(\d+)$/;

    public readonly name = "Discogs";

    constructor(
        private readonly discogsDatabaseService: DiscogsDatabaseService
    ) {}

    canEnrich(mbArtist: IArtist): boolean {
        return this.getDiscogsId(mbArtist) != null;
    }

    async enrich(mbArtist: IArtist): Promise<ProposedArtistEdit[]> {
        if (!this.canEnrich(mbArtist)) {
            throw new TypeError("Cannot enrich this artist!");
        }

        const discogsId = this.getDiscogsId(mbArtist)!;
        const discogsArtist = await this.discogsDatabaseService.getArtist(
            discogsId
        );
        if (discogsArtist == null) {
            DiscogsArtistEnricherService.logger.debug(
                `Could not find discogs artist by id.`
            );
            return [];
        }
        DiscogsArtistEnricherService.logger.silly(
            `Found discogs artist by id.`
        );

        const proposedEdits = [];

        const proposedLegalNameEdit = this.enrichLegalNameFromDiscogs(
            mbArtist,
            discogsArtist
        );
        if (proposedLegalNameEdit != null) {
            proposedEdits.push(proposedLegalNameEdit);
        }
        return proposedEdits;
    }

    private enrichLegalNameFromDiscogs(
        mbArtist: IArtist,
        discogsArtist: DiscogsArtist
    ): ProposedArtistEdit | null {
        const aliases = mbArtist.aliases ?? [];
        const mbLegalNames = aliases.filter(
            alias => alias.type === "Legal name"
        );
        const discogsLegalName = discogsArtist.realname;

        if (discogsLegalName == null) {
            DiscogsArtistEnricherService.logger.debug(
                `No Discogs name found for'${mbArtist.name}'.`
            );
            return null;
        }

        if (discogsLegalName === mbArtist.name) {
            DiscogsArtistEnricherService.logger.debug(
                `Legal name ${discogsLegalName} is already used as main name '${mbArtist.name}'.`
            );
            return null;
        } else if (mbLegalNames.length === 0) {
            DiscogsArtistEnricherService.logger.debug(
                `Found new legal name ${discogsLegalName} for Musicbrainz artist '${mbArtist.name}'.`
            );
            return {
                type: EditType.ADD,
                target: mbArtist,
                property: "legal name",
                new: discogsLegalName
            };
        }
        const differentLegalNames = mbLegalNames.filter(
            alias => alias.name !== discogsLegalName
        );
        if (differentLegalNames.length > 0) {
            DiscogsArtistEnricherService.logger.debug(
                `Found legal name '${discogsLegalName}' that is different from existing '${differentLegalNames.map(
                    alias => alias.name
                )}'.`
            );
            return {
                type: EditType.CHANGE,
                target: mbArtist,
                property: "legal name",
                old: mbLegalNames.map(alias => alias.name),
                new: discogsLegalName
            };
        }
        return null;
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

        const exec = DiscogsArtistEnricherService.DISCOGS_URL_ID_PATTERN.exec(
            discogsRelation.url.resource
        );
        if (exec == null) {
            return null;
        }

        return exec[1];
    }
}

export { DiscogsArtistEnricherService };
