import { Duration } from "moment";
declare const WATER_INITIAL = 100;
declare const WATER_MIN = 0.1;
declare const WATER_MAX = 150;
declare const HAPPINESS_INITIAL = 100;
declare const HAPPINESS_MIN = 0.1;
declare const HAPPINESS_MAX = 100;
declare enum LisaDeathCause {
    DROWNING = "drowning",
    DEHYDRATION = "dehydration",
    SADNESS = "sadness",
    FIRE = "fire"
}
interface LisaLife {
    time: Date;
    byUser: string;
}
interface LisaDeath {
    time: Date | null;
    byUser: string | null;
    cause: LisaDeathCause | null;
}
interface LisaState {
    status: {
        water: number;
        happiness: number;
    };
    life: LisaLife;
    death: LisaDeath;
    bestLifetime: Duration;
}
export { LisaState, LisaDeathCause, WATER_INITIAL, WATER_MIN, WATER_MAX, HAPPINESS_INITIAL, HAPPINESS_MIN, HAPPINESS_MAX };
