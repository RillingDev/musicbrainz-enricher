import { IArtist, IFormData, IMusicBrainzConfig } from "musicbrainz-api";
import { AsyncService } from "../../util/AsyncService";
declare class MusicbrainzDatabaseService {
    private readonly asyncService;
    private static readonly logger;
    private client;
    constructor(musicbrainzConfig: IMusicBrainzConfig, asyncService: AsyncService);
    getArtist(mbId: string): Promise<IArtist>;
    searchArtist(formData: IFormData, consumer: (artist: IArtist) => Promise<void>): Promise<void>;
}
export { MusicbrainzDatabaseService };
