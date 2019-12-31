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
const disconnect_1 = require("disconnect");
const chevron_1 = require("../../chevron");
let DiscogsDatabaseService = class DiscogsDatabaseService {
    constructor() {
        this.database = new disconnect_1.Client().database();
    }
    getArtist(id) {
        return this.database.getArtist(String(id));
    }
};
DiscogsDatabaseService = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron),
    __metadata("design:paramtypes", [])
], DiscogsDatabaseService);
exports.DiscogsDatabaseService = DiscogsDatabaseService;
