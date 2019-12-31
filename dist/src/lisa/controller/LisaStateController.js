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
var LisaStateController_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const lodash_1 = require("lodash");
const moment_1 = require("moment");
const rxjs_1 = require("rxjs");
const chevron_1 = require("../../chevron");
const logger_1 = require("../../logger");
const LisaState_1 = require("../LisaState");
const LisaStatusService_1 = require("../service/LisaStatusService");
let LisaStateController = LisaStateController_1 = class LisaStateController {
    constructor(lisaStatusService) {
        this.lisaStatusService = lisaStatusService;
        this.state = LisaStateController_1.createNewLisaState(LisaStateController_1.USER_SYSTEM, moment_1.duration(0));
        this.stateChangeSubject = new rxjs_1.Subject();
        rxjs_1.interval(LisaStateController_1.BEST_LIFETIME_CHECK_TIMEOUT).subscribe(() => this.updateBestLifetimeIfRequired());
    }
    static createNewLisaState(createdByUser, bestLifetime) {
        return {
            bestLifetime,
            status: {
                water: LisaState_1.WATER_INITIAL,
                happiness: LisaState_1.HAPPINESS_INITIAL
            },
            life: {
                time: new Date(),
                byUser: createdByUser
            },
            death: {
                time: null,
                byUser: null,
                cause: null
            }
        };
    }
    /**
     * Gets a copy of the state to process e.g. when creating text for the current status.
     *
     * @return copy of the current state.
     */
    getStateCopy() {
        return lodash_1.cloneDeep(this.state);
    }
    /**
     * Only used for loading persisted data, do not use for regular state changes.
     *
     * @param state State to load.
     */
    loadState(state) {
        this.state = state;
        this.stateChanged();
    }
    replantLisa(byUser = LisaStateController_1.USER_SYSTEM) {
        LisaStateController_1.logger.debug(`'${byUser}' replanted lisa.`);
        this.performReplant(byUser);
        this.stateChanged();
    }
    killLisa(cause, byUser = LisaStateController_1.USER_SYSTEM) {
        if (!this.lisaStatusService.isAlive(this.getStateCopy())) {
            LisaStateController_1.logger.silly("Lisa is already dead, skip kill.");
            return;
        }
        LisaStateController_1.logger.debug(`'${byUser}' killed lisa by ${cause}.`);
        this.performKill(cause, byUser);
        this.stateChanged();
    }
    modifyLisaStatus(waterModifier, happinessModifier, byUser = LisaStateController_1.USER_SYSTEM) {
        if (!this.lisaStatusService.isAlive(this.getStateCopy())) {
            LisaStateController_1.logger.silly("Lisa is dead, skip status change.");
            return;
        }
        LisaStateController_1.logger.silly(`'${byUser}' modified status; water modifier ${waterModifier}, happiness modifier ${happinessModifier}.`);
        this.performModifyStatus(waterModifier, happinessModifier, byUser);
        this.stateChanged();
    }
    performReplant(byUser) {
        this.state = LisaStateController_1.createNewLisaState(byUser, this.state.bestLifetime);
    }
    performKill(cause, byUser) {
        this.state.death = { time: new Date(), byUser, cause };
    }
    performModifyStatus(waterModifier, happinessModifier, byUser) {
        this.state.status.water += waterModifier;
        this.state.status.happiness += happinessModifier;
        this.checkStats(byUser);
    }
    checkStats(byUser) {
        if (this.state.status.water > LisaState_1.WATER_MAX) {
            LisaStateController_1.logger.silly(`Water level ${this.state.status.water} is above limit of ${LisaState_1.WATER_MAX} -> ${LisaState_1.LisaDeathCause.DROWNING}.`);
            this.performKill(LisaState_1.LisaDeathCause.DROWNING, byUser);
        }
        else if (this.state.status.water < LisaState_1.WATER_MIN) {
            LisaStateController_1.logger.silly(`Water level ${this.state.status.water} is below limit of ${LisaState_1.WATER_MIN} -> ${LisaState_1.LisaDeathCause.DEHYDRATION}.`);
            this.performKill(LisaState_1.LisaDeathCause.DEHYDRATION, byUser);
        }
        if (this.state.status.happiness > LisaState_1.HAPPINESS_MAX) {
            LisaStateController_1.logger.silly(`Happiness level ${this.state.status.happiness} is above limit of ${LisaState_1.HAPPINESS_MAX} -> reducing to limit.`);
            this.state.status.happiness = LisaState_1.HAPPINESS_MAX;
        }
        else if (this.state.status.happiness < LisaState_1.HAPPINESS_MIN) {
            LisaStateController_1.logger.silly(`Happiness level ${this.state.status.happiness} is below limit of ${LisaState_1.HAPPINESS_MIN} -> ${LisaState_1.LisaDeathCause.SADNESS}.`);
            this.performKill(LisaState_1.LisaDeathCause.SADNESS, byUser);
        }
    }
    stateChanged() {
        LisaStateController_1.logger.silly("Lisa state changed.");
        this.stateChangeSubject.next(this.getStateCopy());
    }
    updateBestLifetimeIfRequired() {
        const lifetime = this.lisaStatusService.getLifetime(this.getStateCopy());
        if (lifetime > this.state.bestLifetime) {
            LisaStateController_1.logger.silly(`Increasing high score from ${this.state.bestLifetime} to ${lifetime}.`);
            this.state.bestLifetime = lifetime;
        }
    }
};
LisaStateController.logger = logger_1.rootLogger.child({
    target: LisaStateController_1
});
LisaStateController.USER_SYSTEM = "System";
LisaStateController.BEST_LIFETIME_CHECK_TIMEOUT = 5000;
LisaStateController = LisaStateController_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [LisaStatusService_1.LisaStatusService]
    }),
    __metadata("design:paramtypes", [LisaStatusService_1.LisaStatusService])
], LisaStateController);
exports.LisaStateController = LisaStateController;
