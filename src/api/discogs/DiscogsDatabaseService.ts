import { Injectable } from "chevronjs";
import { Client as DiscogsClient } from "disconnect";
import { chevron } from "../../chevron";
import { DiscogsConfig, discogsConfigInjectableName } from "../../config.js";
import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";

@Injectable(chevron, {
    dependencies: [discogsConfigInjectableName, AsyncService]
})
class DiscogsDatabaseService {
    private database: any;

    constructor(
        discogsConfig: DiscogsConfig,
        private readonly asyncService: AsyncService
    ) {
        this.database = new DiscogsClient(
            discogsConfig.userAgent,
            discogsConfig.auth
        ).database();
    }

    public async getArtist(id: string): Promise<DiscogsArtist> {
        // TODO add proper rate limit handling
        await this.asyncService.throttle(500);
        return this.database.getArtist(String(id));
    }
}

export { DiscogsDatabaseService };
