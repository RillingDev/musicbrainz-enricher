import { Subject } from "rxjs";
import { LisaDeathCause, LisaState } from "../LisaState";
import { LisaStatusService } from "../service/LisaStatusService";
declare class LisaStateController {
    private readonly lisaStatusService;
    private static readonly logger;
    private static readonly USER_SYSTEM;
    private static readonly BEST_LIFETIME_CHECK_TIMEOUT;
    readonly stateChangeSubject: Subject<LisaState>;
    private state;
    constructor(lisaStatusService: LisaStatusService);
    private static createNewLisaState;
    /**
     * Gets a copy of the state to process e.g. when creating text for the current status.
     *
     * @return copy of the current state.
     */
    getStateCopy(): LisaState;
    /**
     * Only used for loading persisted data, do not use for regular state changes.
     *
     * @param state State to load.
     */
    loadState(state: LisaState): void;
    replantLisa(byUser?: string): void;
    killLisa(cause: LisaDeathCause, byUser?: string): void;
    modifyLisaStatus(waterModifier: number, happinessModifier: number, byUser?: string): void;
    private performReplant;
    private performKill;
    private performModifyStatus;
    private checkStats;
    private stateChanged;
    private updateBestLifetimeIfRequired;
}
export { LisaStateController };
