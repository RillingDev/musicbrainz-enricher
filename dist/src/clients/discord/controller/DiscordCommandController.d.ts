import { User } from "discord.js";
import { LisaStateController } from "../../../lisa/controller/LisaStateController";
import { LisaDeathCause } from "../../../lisa/LisaState";
import { LisaStatusService } from "../../../lisa/service/LisaStatusService";
import { LisaTextService } from "../../../lisa/service/LisaTextService";
import { DiscordService } from "../service/DiscordService";
declare class DiscordCommandController {
    private readonly lisaStateController;
    private readonly lisaStatusService;
    private readonly lisaTextService;
    private readonly lisaDiscordService;
    constructor(lisaStateController: LisaStateController, lisaStatusService: LisaStatusService, lisaTextService: LisaTextService, lisaDiscordService: DiscordService);
    performAction(author: User, waterModifier: number, happinessModifier: number, allowedUserIds: string[] | null, textSuccess: string[], textDead: string[], textNotAllowed?: string[]): string;
    performKill(author: User, cause: LisaDeathCause, allowedUserIds: string[] | null, textSuccess: string[], textAlreadyDead: string[], textNotAllowed?: string[]): string;
    performReplant(author: User, allowedUserIds: string[] | null, textWasAlive: string[], textWasDead: string[], textNotAllowed?: string[]): string;
    createStatusText(): string;
    private isAlive;
}
export { DiscordCommandController };
