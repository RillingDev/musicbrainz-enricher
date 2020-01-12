import { Injectable } from "chevronjs";
import {
    IArtist,
    IArtistList,
    IFormData,
    MusicBrainzApi
} from "musicbrainz-api";
import { ISearchResult } from "musicbrainz-api/lib/musicbrainz.types.js";
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
        await this.fetchAll<IArtist>(
            offset => client.searchArtist(formData, offset),
            result => (<IArtistList>result).artists,
            consumer
        );
    }

    private async fetchAll<T>(
        searchProvider: (offset: number) => Promise<ISearchResult>,
        itemListExtractor: (result: ISearchResult) => T[],
        consumer: (value: T) => Promise<void>
    ): Promise<void> {
        let offset = 0;
        let totalCount: number | null = null;
        do {
            const response = await searchProvider(offset);
            if (totalCount == null) {
                totalCount = response.count;
            }
            const itemList = itemListExtractor(response);
            offset += itemList.length;
            await this.asyncService.queue(
                itemList.map(item => (): Promise<void> => consumer(item))
            );
        } while (totalCount != null && offset < totalCount);
    }

    private async createClient(): Promise<MusicBrainzApi> {
        return new MusicBrainzApi(
            await this.musicbrainzConfigProvider.getInstance()
        );
    }
}

export { MusicbrainzDatabaseService };
