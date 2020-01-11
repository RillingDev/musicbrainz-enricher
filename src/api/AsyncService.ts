import { Injectable } from "chevronjs";
import { chevron } from "../chevron.js";

@Injectable(chevron)
class AsyncService {
    public throttle(timeout: number): Promise<void> {
        return new Promise(resolve => setTimeout(resolve, timeout));
    }

    public async queue<T>(
        promiseProducers: Array<() => Promise<T>>
    ): Promise<void> {
        for (const promiseProducer of promiseProducers) {
            await promiseProducer();
        }
    }
}

export { AsyncService };
