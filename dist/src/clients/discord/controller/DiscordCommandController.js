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
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const lodash_1 = require("lodash");
const chevron_1 = require("../../../chevron");
const LisaStateController_1 = require("../../../lisa/controller/LisaStateController");
const LisaStatusService_1 = require("../../../lisa/service/LisaStatusService");
const LisaTextService_1 = require("../../../lisa/service/LisaTextService");
const DiscordService_1 = require("../service/DiscordService");
let DiscordCommandController = class DiscordCommandController {
    constructor(lisaStateController, lisaStatusService, lisaTextService, lisaDiscordService) {
        this.lisaStateController = lisaStateController;
        this.lisaStatusService = lisaStatusService;
        this.lisaTextService = lisaTextService;
        this.lisaDiscordService = lisaDiscordService;
    }
    performAction(author, waterModifier, happinessModifier, allowedUserIds, textSuccess, textDead, textNotAllowed = []) {
        if (!this.lisaDiscordService.isUserAllowed(allowedUserIds, author)) {
            return lodash_1.sample(textNotAllowed);
        }
        if (!this.isAlive()) {
            return lodash_1.sample(textDead);
        }
        this.lisaStateController.modifyLisaStatus(waterModifier, happinessModifier, this.lisaDiscordService.getFullUserName(author));
        return [lodash_1.sample(textSuccess), this.createStatusText()].join("\n");
    }
    performKill(author, cause, allowedUserIds, textSuccess, textAlreadyDead, textNotAllowed = []) {
        if (!this.lisaDiscordService.isUserAllowed(allowedUserIds, author)) {
            return lodash_1.sample(textNotAllowed);
        }
        if (!this.isAlive()) {
            return lodash_1.sample(textAlreadyDead);
        }
        this.lisaStateController.killLisa(cause, this.lisaDiscordService.getFullUserName(author));
        return [lodash_1.sample(textSuccess), this.createStatusText()].join("\n");
    }
    performReplant(author, allowedUserIds, textWasAlive, textWasDead, textNotAllowed = []) {
        if (!this.lisaDiscordService.isUserAllowed(allowedUserIds, author)) {
            return lodash_1.sample(textNotAllowed);
        }
        const wasAlive = this.isAlive();
        this.lisaStateController.replantLisa(this.lisaDiscordService.getFullUserName(author));
        return lodash_1.sample(wasAlive ? textWasAlive : textWasDead);
    }
    createStatusText() {
        return this.lisaTextService.createStatusText(this.lisaStateController.getStateCopy());
    }
    isAlive() {
        return this.lisaStatusService.isAlive(this.lisaStateController.getStateCopy());
    }
};
DiscordCommandController = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [
            LisaStateController_1.LisaStateController,
            LisaStatusService_1.LisaStatusService,
            LisaTextService_1.LisaTextService,
            DiscordService_1.DiscordService
        ]
    }),
    __metadata("design:paramtypes", [LisaStateController_1.LisaStateController,
        LisaStatusService_1.LisaStatusService,
        LisaTextService_1.LisaTextService,
        DiscordService_1.DiscordService])
], DiscordCommandController);
exports.DiscordCommandController = DiscordCommandController;
