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
var LisaStateStorageController_1;
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const operators_1 = require("rxjs/operators");
const chevron_1 = require("../../chevron");
const logger_1 = require("../../logger");
const JsonStorageService_1 = require("../service/JsonStorageService");
const LisaStateStorageService_1 = require("../service/LisaStateStorageService");
let LisaStateStorageController = LisaStateStorageController_1 = class LisaStateStorageController {
    constructor(jsonStorageService, lisaStateStorageService) {
        this.jsonStorageService = jsonStorageService;
        this.lisaStateStorageService = lisaStateStorageService;
    }
    bindStateChangeSubscription(stateChangeSubject) {
        stateChangeSubject
            .pipe(operators_1.throttleTime(LisaStateStorageController_1.STORAGE_THROTTLE_TIMEOUT))
            .subscribe(state => {
            this.storeState(state).catch(e => LisaStateStorageController_1.logger.error("Could not save state!", e));
        });
    }
    async hasStoredState() {
        return this.jsonStorageService.hasStorageKey(LisaStateStorageController_1.STORAGE_PATH, LisaStateStorageController_1.STORAGE_KEY);
    }
    async loadStoredState() {
        const storedState = await this.jsonStorageService.load(LisaStateStorageController_1.STORAGE_PATH, LisaStateStorageController_1.STORAGE_KEY);
        return this.lisaStateStorageService.fromStorable(storedState);
    }
    async storeState(state) {
        const jsonLisaState = this.lisaStateStorageService.toStorable(state);
        return await this.jsonStorageService.store(LisaStateStorageController_1.STORAGE_PATH, LisaStateStorageController_1.STORAGE_KEY, jsonLisaState);
    }
};
LisaStateStorageController.STORAGE_THROTTLE_TIMEOUT = 10000;
LisaStateStorageController.STORAGE_PATH = "data/storage.json";
LisaStateStorageController.STORAGE_KEY = "lisaState";
LisaStateStorageController.logger = logger_1.rootLogger.child({
    target: LisaStateStorageController_1
});
LisaStateStorageController = LisaStateStorageController_1 = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron, {
        dependencies: [JsonStorageService_1.JsonStorageService, LisaStateStorageService_1.LisaStateStorageService]
    }),
    __metadata("design:paramtypes", [JsonStorageService_1.JsonStorageService,
        LisaStateStorageService_1.LisaStateStorageService])
], LisaStateStorageController);
exports.LisaStateStorageController = LisaStateStorageController;
