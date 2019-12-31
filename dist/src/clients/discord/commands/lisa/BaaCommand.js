"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
const GOAT_IDS = [
    "169804264988868609",
    "178470784984023040",
    "143158243076734986",
    "128985967875850240",
    "273221196001181697"
];
class BaaCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "baa",
            aliases: [],
            group: "lisa",
            memberName: "baa",
            description: "Baa.",
            hidden: true
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performAction(message.author, 0, 30, GOAT_IDS, ["Baa", "Baa~", "Baaaaaaa ^w^", ":goat:"], ["Baa? a dead Lisa..."], ["You're not a goat uwu"]));
    }
}
exports.BaaCommand = BaaCommand;
