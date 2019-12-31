import { DiscogsArtist } from "./schema/DiscogsArtist";
declare class DiscogsDatabaseService {
    private database;
    constructor();
    getArtist(id: string): Promise<DiscogsArtist>;
}
export { DiscogsDatabaseService };
