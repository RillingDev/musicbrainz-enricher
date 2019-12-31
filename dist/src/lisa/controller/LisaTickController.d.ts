import { Observable } from "rxjs";
interface TickData {
    waterModifier: number;
    happinessModifier: number;
    byUser: string;
}
declare class LisaTickController {
    private static readonly logger;
    private static readonly TIMEOUT;
    private static readonly WATER_MODIFIER;
    private static readonly HAPPINESS_MODIFIER;
    private static readonly USER_TICK;
    readonly tickObservable: Observable<TickData>;
    constructor();
    private createTickObservable;
}
export { LisaTickController };
