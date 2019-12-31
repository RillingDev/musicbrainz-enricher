import { Injectable } from "chevronjs";
import { IArtist, IFormData, IMusicBrainzConfig, MusicBrainzApi } from "musicbrainz-api";
import { chevron } from "../../chevron";

@Injectable(chevron, { dependencies: ["musicbrainzConfig"] })
class MusicbrainzDatabaseService {
    private client: MusicBrainzApi;

    constructor(musicbrainzConfig: IMusicBrainzConfig) {
        this.client = new MusicBrainzApi(musicbrainzConfig);
    }

    public getArtist(mbId: string): Promise<IArtist> {
        return this.client.getArtist(mbId, ["aliases", "url-rels"]);
    }

    public search(formData: IFormData) {
        return this.client.searchArtist(formData);
    }
}

export { MusicbrainzDatabaseService };
