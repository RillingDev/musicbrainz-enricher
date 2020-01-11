"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var DiscogsDatabaseService_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const disconnect_1 = require("disconnect");
const chevron_1 = require("../../chevron");
const config_js_1 = require("../../config.js");
const logger_js_1 = require("../../logger.js");
const AsyncService_1 = require("../../util/AsyncService");
const pRetry = require("p-retry");
let DiscogsDatabaseService = DiscogsDatabaseService_1 = class DiscogsDatabaseService {
    constructor(discogsConfig, asyncService) {
        this.asyncService = asyncService;
        this.database = new disconnect_1.Client().database();
    }
    getArtist(id) {
        return this.request(() => this.database.getArtist(id));
    }
    async request(requestProducer) {
        return pRetry(async () => {
            try {
                return await requestProducer();
            }
            catch (err) {
                // If anything but the rate limit exceeded error appears,
                // Signal that no retry should happen.
                if (err.statusCode ===
                    DiscogsDatabaseService_1.RATE_LIMIT_EXCEEDED_STATUS_CODE) {
                    throw err;
                }
                else {
                    throw new pRetry.AbortError(err);
                }
            }
        }, {
            onFailedAttempt: async (err) => {
                DiscogsDatabaseService_1.logger.warn(`Hit rate limit, waiting ${DiscogsDatabaseService_1.RETRY_TIMEOUT}ms before retry: ${err}`);
                await this.asyncService.throttle(DiscogsDatabaseService_1.RETRY_TIMEOUT);
            },
            retries: DiscogsDatabaseService_1.RETRY_MAX
        });
    }
};
DiscogsDatabaseService.logger = logger_js_1.rootLogger.child({
    target: DiscogsDatabaseService_1
});
DiscogsDatabaseService.RETRY_TIMEOUT = 10000;
DiscogsDatabaseService.RETRY_MAX = 6;
DiscogsDatabaseService.RATE_LIMIT_EXCEEDED_STATUS_CODE = 429;
DiscogsDatabaseService = DiscogsDatabaseService_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [config_js_1.discogsConfigInjectableName, AsyncService_1.AsyncService]
    }),
    __metadata("design:paramtypes", [Object, AsyncService_1.AsyncService])
], DiscogsDatabaseService);
exports.DiscogsDatabaseService = DiscogsDatabaseService;
