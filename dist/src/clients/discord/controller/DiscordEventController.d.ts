import { LisaStateController } from "../../../lisa/controller/LisaStateController";
import { LisaTextService } from "../../../lisa/service/LisaTextService";
import { DiscordClient } from "../DiscordClient";
declare class DiscordEventController {
    private readonly lisaStateController;
    private readonly lisaDiscordClient;
    private readonly lisaTextService;
    private static readonly logger;
    private static readonly PRESENCE_UPDATE_THROTTLE_TIMEOUT;
    private static readonly MESSAGE_THROTTLE_TIMEOUT;
    private static readonly MESSAGE_HAPPINESS_MODIFIER;
    private static readonly USER_DISCORD_ACTIVITY;
    constructor(lisaStateController: LisaStateController, lisaDiscordClient: DiscordClient, lisaTextService: LisaTextService);
    bindListeners(): void;
    private onMessage;
    private onStateChange;
}
export { DiscordEventController };
