import { Message } from "discord.js";
import { Command, CommandoClient, CommandoMessage } from "discord.js-commando";
declare class BurnCommand extends Command {
    private readonly lisaDiscordCommandController;
    constructor(client: CommandoClient);
    run(message: CommandoMessage): Promise<Message | Message[]>;
}
export { BurnCommand };
