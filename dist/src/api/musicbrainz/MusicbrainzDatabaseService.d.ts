import { IArtist, IFormData, IMusicBrainzConfig } from "musicbrainz-api";
declare class MusicbrainzDatabaseService {
    private client;
    constructor(musicbrainzConfig: IMusicBrainzConfig);
    getArtist(mbId: string): Promise<IArtist>;
    search(formData: IFormData): Promise<import("musicbrainz-api").IArtistList>;
}
export { MusicbrainzDatabaseService };
