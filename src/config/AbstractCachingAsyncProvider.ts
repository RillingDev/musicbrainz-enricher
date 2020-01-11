import { AsyncProvider } from "./AsyncProvider.js";

export abstract class AbstractCachingAsyncProvider<T>
    implements AsyncProvider<T> {
    private instance: T | null = null;

    public async getInstance(): Promise<T> {
        if (this.instance == null) {
            this.instance = await this.createInstance();
        }
        return this.instance;
    }

    protected abstract async createInstance(): Promise<T>;
}
