import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";
declare class DiscogsDatabaseService {
    private readonly asyncService;
    private database;
    constructor(asyncService: AsyncService);
    getArtist(id: string): Promise<DiscogsArtist>;
}
export { DiscogsDatabaseService };
