import { LisaState } from "../LisaState";
import { LisaStatusService } from "./LisaStatusService";
declare class LisaTextService {
    private readonly lisaStatusService;
    constructor(lisaStatusService: LisaStatusService);
    createStatusText(state: LisaState): string;
    createStatusLabel(state: LisaState): string;
    private createScoreText;
}
export { LisaTextService };
