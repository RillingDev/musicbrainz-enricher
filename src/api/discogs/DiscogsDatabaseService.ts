import { Injectable } from "chevronjs";
import { Client as DiscogsClient } from "disconnect";
import { chevron } from "../../chevron";
import { DiscogsConfigProvider } from "../../config/DiscogsConfigProvider.js";
import { rootLogger } from "../../logger.js";
import { AsyncService } from "../AsyncService.js";
import { DiscogsArtist } from "./schema/DiscogsArtist";
import pRetry = require("p-retry");

@Injectable(chevron, {
    dependencies: [DiscogsConfigProvider, AsyncService]
})
class DiscogsDatabaseService {
    private static readonly logger = rootLogger.child({
        target: DiscogsDatabaseService
    });
    private static readonly RETRY_TIMEOUT = 10000;
    private static readonly RETRY_MAX = 6;
    private static readonly RATE_LIMIT_EXCEEDED_STATUS_CODE = 429;

    constructor(
        private readonly discogsConfigProvider: DiscogsConfigProvider,
        private readonly asyncService: AsyncService
    ) {}

    public async getArtist(id: string): Promise<DiscogsArtist> {
        const database = (await this.createDiscogsClient()).database();
        return this.request<DiscogsArtist>(() => database.getArtist(id));
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

    private async createDiscogsClient(): Promise<any> {
        const {
            userAgent,
            auth
        } = await this.discogsConfigProvider.getInstance();
        return new DiscogsClient(userAgent, auth);
    }
}

export { DiscogsDatabaseService };
