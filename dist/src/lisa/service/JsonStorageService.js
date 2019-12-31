"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const fs_extra_1 = require("fs-extra");
const chevron_1 = require("../../chevron");
let JsonStorageService = class JsonStorageService {
    async hasStorageKey(path, key) {
        return (await this.load(path, key)) != null;
    }
    async load(path, key) {
        if (!(await this.hasStorage(path))) {
            return null;
        }
        const object = await this.loadAll(path);
        if (!(key in object)) {
            return null;
        }
        return object[key];
    }
    async store(path, key, data) {
        if (!(await this.hasStorage(path))) {
            await this.initStorage(path);
        }
        const object = await this.loadAll(path);
        object[key] = data;
        return await fs_extra_1.writeJSON(path, object);
    }
    async hasStorage(path) {
        return fs_extra_1.pathExists(path);
    }
    async loadAll(path) {
        return await fs_extra_1.readJSON(path);
    }
    async initStorage(path) {
        return await fs_extra_1.writeJSON(path, {});
    }
};
JsonStorageService = __decorate([
    chevronjs_1.Injectable(chevron_1.chevron)
], JsonStorageService);
exports.JsonStorageService = JsonStorageService;
