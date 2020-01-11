import { DiscogsConfig } from "../../config.js";
import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";
declare class DiscogsDatabaseService {
    private readonly asyncService;
    private static readonly logger;
    private static readonly RETRY_TIMEOUT;
    private static readonly RETRY_MAX;
    private static readonly RATE_LIMIT_EXCEEDED_STATUS_CODE;
    private readonly database;
    constructor(discogsConfig: DiscogsConfig, asyncService: AsyncService);
    getArtist(id: string): Promise<DiscogsArtist>;
    private request;
}
export { DiscogsDatabaseService };
