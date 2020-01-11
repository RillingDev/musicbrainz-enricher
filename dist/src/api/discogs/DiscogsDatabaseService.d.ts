import { DiscogsConfig } from "../../config.js";
import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";
declare class DiscogsDatabaseService {
    private readonly asyncService;
    private database;
    constructor(discogsConfig: DiscogsConfig, asyncService: AsyncService);
    getArtist(id: string): Promise<DiscogsArtist>;
}
export { DiscogsDatabaseService };
