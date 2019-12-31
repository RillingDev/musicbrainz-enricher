"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
const MISSY_ID = ["273221196001181697"];
class MissyCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "missy",
            aliases: [],
            group: "lisa",
            memberName: "missy",
            description: "baaff",
            hidden: true
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performAction(message.author, 0, 40, MISSY_ID, ["_Baaaaaaaaaaaaaa_"], ["OwO whats this? a dead Lisa..."], ["You're not a missy <w<"]));
    }
}
exports.MissyCommand = MissyCommand;
