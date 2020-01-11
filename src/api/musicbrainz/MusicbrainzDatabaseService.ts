import { Injectable } from "chevronjs";
import {
    IArtist,
    IFormData,
    IMusicBrainzConfig,
    MusicBrainzApi
} from "musicbrainz-api";
import { chevron } from "../../chevron";
import { musicbrainzConfigInjectableName } from "../../config.js";
import { rootLogger } from "../../logger";
import { AsyncService } from "../../util/AsyncService";

@Injectable(chevron, { dependencies: [musicbrainzConfigInjectableName, AsyncService] })
class MusicbrainzDatabaseService {
    private static readonly logger = rootLogger.child({
        target: MusicbrainzDatabaseService
    });

    private client: MusicBrainzApi;

    constructor(
        musicbrainzConfig: IMusicBrainzConfig,
        private readonly asyncService: AsyncService
    ) {
        this.client = new MusicBrainzApi(musicbrainzConfig);
    }

    public async getArtist(mbId: string): Promise<IArtist> {
        return this.client.getArtist(mbId, ["aliases", "url-rels"]);
    }

    public async searchArtist(
        formData: IFormData,
        consumer: (artist: IArtist) => Promise<void>
    ): Promise<void> {
        let offset = 0;
        let totalCount: number;
        do {
            MusicbrainzDatabaseService.logger.debug(
                `Searching artist with form data '${JSON.stringify(
                    formData
                )}' and offset ${offset}.`
            );
            const response = await this.client.searchArtist(formData, offset);
            totalCount = response.count;
            offset += response.artists.length;
            await this.asyncService.queue(
                response.artists.map(artist => (): Promise<void> =>
                    consumer(artist)
                )
            );
        } while (offset < totalCount);
    }
}

export { MusicbrainzDatabaseService };
