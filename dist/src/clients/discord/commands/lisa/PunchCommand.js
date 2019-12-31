"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
class PunchCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "punch",
            aliases: ["hit"],
            group: "lisa",
            memberName: "punch",
            description: "Punch Lisa."
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performAction(message.author, 0, -10, null, ["_Is being punched in the leaves._", "oof.", "ouch ouw owie."], ["The dead feel no pain..."]));
    }
}
exports.PunchCommand = PunchCommand;
