import { Injectable } from "chevronjs";
import { IArtist, IFormData, MusicBrainzApi } from "musicbrainz-api";
import { chevron } from "../../chevron";
import { MusicbrainzConfigProvider } from "../../config/MusicbrainzConfigProvider.js";
import { rootLogger } from "../../logger";
import { AsyncService } from "../AsyncService.js";

@Injectable(chevron, {
    dependencies: [MusicbrainzConfigProvider, AsyncService]
})
class MusicbrainzDatabaseService {
    private static readonly logger = rootLogger.child({
        target: MusicbrainzDatabaseService
    });

    constructor(
        private readonly musicbrainzConfigProvider: MusicbrainzConfigProvider,
        private readonly asyncService: AsyncService
    ) {}

    public async getArtist(mbId: string): Promise<IArtist> {
        const client = await this.createClient();
        return client.getArtist(mbId, ["aliases", "url-rels"]);
    }

    public async searchArtist(
        formData: IFormData,
        consumer: (artist: IArtist) => Promise<void>
    ): Promise<void> {
        const client = await this.createClient();
        let offset = 0;
        let totalCount: number;
        do {
            MusicbrainzDatabaseService.logger.debug(
                `Searching artist with form data '${JSON.stringify(
                    formData
                )}' and offset ${offset}.`
            );
            const response = await client.searchArtist(formData, offset);
            totalCount = response.count;
            offset += response.artists.length;
            await this.asyncService.queue(
                response.artists.map(artist => (): Promise<void> =>
                    consumer(artist)
                )
            );
        } while (offset < totalCount);
    }

    private async createClient(): Promise<MusicBrainzApi> {
        return new MusicBrainzApi(
            await this.musicbrainzConfigProvider.getInstance()
        );
    }
}

export { MusicbrainzDatabaseService };
