import { IArtist, IFormData } from "musicbrainz-api";
import { MusicbrainzConfigProvider } from "../../config/MusicbrainzConfigProvider.js";
import { AsyncService } from "../AsyncService.js";
declare class MusicbrainzDatabaseService {
    private readonly musicbrainzConfigProvider;
    private readonly asyncService;
    private static readonly logger;
    constructor(musicbrainzConfigProvider: MusicbrainzConfigProvider, asyncService: AsyncService);
    getArtist(mbId: string): Promise<IArtist>;
    searchArtist(formData: IFormData, consumer: (artist: IArtist) => Promise<void>): Promise<void>;
    private createClient;
}
export { MusicbrainzDatabaseService };
