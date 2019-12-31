import { Subject } from "rxjs";
import { LisaState } from "../LisaState";
import { JsonStorageService } from "../service/JsonStorageService";
import { LisaStateStorageService } from "../service/LisaStateStorageService";
declare class LisaStateStorageController {
    private readonly jsonStorageService;
    private readonly lisaStateStorageService;
    private static readonly STORAGE_THROTTLE_TIMEOUT;
    private static readonly STORAGE_PATH;
    private static readonly STORAGE_KEY;
    private static readonly logger;
    constructor(jsonStorageService: JsonStorageService, lisaStateStorageService: LisaStateStorageService);
    bindStateChangeSubscription(stateChangeSubject: Subject<LisaState>): void;
    hasStoredState(): Promise<boolean>;
    loadStoredState(): Promise<LisaState>;
    storeState(state: LisaState): Promise<void>;
}
export { LisaStateStorageController };
