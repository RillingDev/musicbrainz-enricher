import { Injectable } from "chevronjs";
import { Client as DiscogsClient } from "disconnect";
import { chevron } from "../../chevron";
import { DiscogsConfig, discogsConfigInjectableName } from "../../config.js";
import { rootLogger } from "../../logger.js";
import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";
import pRetry = require("p-retry");

@Injectable(chevron, {
    dependencies: [discogsConfigInjectableName, AsyncService]
})
class DiscogsDatabaseService {
    private static readonly logger = rootLogger.child({
        target: DiscogsDatabaseService
    });
    private static readonly RETRY_TIMEOUT = 10000;
    private static readonly RETRY_MAX = 6;
    private static readonly RATE_LIMIT_EXCEEDED_STATUS_CODE = 429;

    private readonly database: any;

    constructor(
        discogsConfig: DiscogsConfig,
        private readonly asyncService: AsyncService
    ) {
        this.database = new DiscogsClient(
            discogsConfig.userAgent,
            discogsConfig.auth
        ).database();
    }

    public getArtist(id: string): Promise<DiscogsArtist> {
        return this.request<DiscogsArtist>(() => this.database.getArtist(id));
    }

    private async request<T>(requestProducer: () => Promise<T>): Promise<T> {
        return pRetry<T>(
            async () => {
                try {
                    return await requestProducer();
                } catch (err) {
                    // If anything but the rate limit exceeded error appears,
                    // Signal that no retry should happen.
                    if (
                        err.statusCode ===
                        DiscogsDatabaseService.RATE_LIMIT_EXCEEDED_STATUS_CODE
                    ) {
                        throw err;
                    } else {
                        throw new pRetry.AbortError(err);
                    }
                }
            },
            {
                onFailedAttempt: async err => {
                    DiscogsDatabaseService.logger.warn(
                        `Hit rate limit, waiting ${DiscogsDatabaseService.RETRY_TIMEOUT}ms before retry: ${err}`
                    );
                    await this.asyncService.throttle(
                        DiscogsDatabaseService.RETRY_TIMEOUT
                    );
                },
                retries: DiscogsDatabaseService.RETRY_MAX
            }
        );
    }
}

export { DiscogsDatabaseService };
