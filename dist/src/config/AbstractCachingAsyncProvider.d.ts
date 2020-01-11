import { AsyncProvider } from "./AsyncProvider.js";
export declare abstract class AbstractCachingAsyncProvider<T> implements AsyncProvider<T> {
    private instance;
    getInstance(): Promise<T>;
    protected abstract createInstance(): Promise<T>;
}
