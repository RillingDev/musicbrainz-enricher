import { Duration } from "moment";
import { LisaState } from "../LisaState";
declare class LisaStatusService {
    private static readonly logger;
    isAlive(state: LisaState): boolean;
    getLifetime(state: LisaState): Duration;
    getTimeSinceDeath(state: LisaState): Duration | null;
    /**
     * Returns an relative index from 0 to 1 how well lisa is doing, where 1 is the best and 0 the worst.
     *
     * @return relative index.
     */
    calculateRelativeIndex(state: LisaState): number;
}
export { LisaStatusService };
