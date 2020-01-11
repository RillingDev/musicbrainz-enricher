import { DiscogsConfigProvider } from "../../config/DiscogsConfigProvider.js";
import { AsyncService } from "../AsyncService.js";
import { DiscogsArtist } from "./schema/DiscogsArtist";
declare class DiscogsDatabaseService {
    private readonly discogsConfigProvider;
    private readonly asyncService;
    private static readonly logger;
    private static readonly RETRY_TIMEOUT;
    private static readonly RETRY_MAX;
    private static readonly RATE_LIMIT_EXCEEDED_STATUS_CODE;
    constructor(discogsConfigProvider: DiscogsConfigProvider, asyncService: AsyncService);
    getArtist(id: string): Promise<DiscogsArtist>;
    private request;
    private createDiscogsClient;
}
export { DiscogsDatabaseService };
