import { Message, PresenceData } from "discord.js";
import { CommandoClientOptions } from "discord.js-commando";
import { Observable } from "rxjs";
declare class DiscordClient {
    private commandoClient;
    constructor();
    init(options: CommandoClientOptions): void;
    login(token: string): Promise<void>;
    setPresence(data: PresenceData): Promise<void>;
    getMessageObservable(): Observable<Message>;
}
export { DiscordClient };
