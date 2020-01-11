"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class AbstractCachingAsyncProvider {
    constructor() {
        this.instance = null;
    }
    async getInstance() {
        if (this.instance == null) {
            this.instance = await this.createInstance();
        }
        return this.instance;
    }
}
exports.AbstractCachingAsyncProvider = AbstractCachingAsyncProvider;
