declare class AsyncService {
    throttle(timeout: number): Promise<void>;
    queue<T>(promiseProducers: Array<() => Promise<T>>): Promise<void>;
}
export { AsyncService };
