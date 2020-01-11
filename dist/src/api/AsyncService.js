"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
const chevronjs_1 = require("chevronjs");
const chevron_js_1 = require("../chevron.js");
let AsyncService = class AsyncService {
    throttle(timeout) {
        return new Promise(resolve => setTimeout(resolve, timeout));
    }
    async queue(promiseProducers) {
        for (const promiseProducer of promiseProducers) {
            await promiseProducer();
        }
    }
};
AsyncService = __decorate([
    chevronjs_1.Injectable(chevron_js_1.chevron)
], AsyncService);
exports.AsyncService = AsyncService;
