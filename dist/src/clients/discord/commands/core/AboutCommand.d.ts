import { Message } from "discord.js";
import { Command, CommandoClient, CommandoMessage } from "discord.js-commando";
declare class AboutCommand extends Command {
    constructor(client: CommandoClient);
    run(message: CommandoMessage): Promise<Message | Message[]>;
}
export { AboutCommand };
