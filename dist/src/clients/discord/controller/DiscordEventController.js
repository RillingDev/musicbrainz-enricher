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
var DiscordEventController_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const operators_1 = require("rxjs/operators");
const chevron_1 = require("../../../chevron");
const LisaStateController_1 = require("../../../lisa/controller/LisaStateController");
const LisaTextService_1 = require("../../../lisa/service/LisaTextService");
const logger_1 = require("../../../logger");
const DiscordClient_1 = require("../DiscordClient");
const createPresence = (name) => {
    return {
        activity: {
            name
        }
    };
};
let DiscordEventController = DiscordEventController_1 = class DiscordEventController {
    constructor(lisaStateController, lisaDiscordClient, lisaTextService) {
        this.lisaStateController = lisaStateController;
        this.lisaDiscordClient = lisaDiscordClient;
        this.lisaTextService = lisaTextService;
    }
    bindListeners() {
        this.lisaDiscordClient
            .getMessageObservable()
            .pipe(operators_1.filter(message => !message.system && !message.author.bot), operators_1.throttleTime(DiscordEventController_1.MESSAGE_THROTTLE_TIMEOUT))
            .subscribe(() => this.onMessage());
        this.lisaStateController.stateChangeSubject
            .pipe(operators_1.throttleTime(DiscordEventController_1.PRESENCE_UPDATE_THROTTLE_TIMEOUT))
            .subscribe(state => this.onStateChange(state));
        this.onStateChange(this.lisaStateController.getStateCopy());
    }
    onMessage() {
        DiscordEventController_1.logger.silly("A message was sent, increasing happiness.");
        this.lisaStateController.modifyLisaStatus(0, DiscordEventController_1.MESSAGE_HAPPINESS_MODIFIER, DiscordEventController_1.USER_DISCORD_ACTIVITY);
    }
    onStateChange(state) {
        const statusLabel = `${this.lisaTextService.createStatusLabel(state)}.`;
        DiscordEventController_1.logger.debug(`Updating presence to '${statusLabel}'...`);
        this.lisaDiscordClient
            .setPresence(createPresence(statusLabel))
            .then(() => DiscordEventController_1.logger.debug("Updated presence."))
            .catch(e => DiscordEventController_1.logger.error("Could not update presence.", e));
    }
};
DiscordEventController.logger = logger_1.rootLogger.child({
    target: DiscordEventController_1
});
DiscordEventController.PRESENCE_UPDATE_THROTTLE_TIMEOUT = 10000;
DiscordEventController.MESSAGE_THROTTLE_TIMEOUT = 1000;
DiscordEventController.MESSAGE_HAPPINESS_MODIFIER = 0.25;
DiscordEventController.USER_DISCORD_ACTIVITY = "Discord activity";
DiscordEventController = DiscordEventController_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [LisaStateController_1.LisaStateController, DiscordClient_1.DiscordClient, LisaTextService_1.LisaTextService]
    }),
    __metadata("design:paramtypes", [LisaStateController_1.LisaStateController,
        DiscordClient_1.DiscordClient,
        LisaTextService_1.LisaTextService])
], DiscordEventController);
exports.DiscordEventController = DiscordEventController;
