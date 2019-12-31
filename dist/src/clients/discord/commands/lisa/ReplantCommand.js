"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
class ReplantCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "replant",
            aliases: ["reset", "plant"],
            group: "lisa",
            memberName: "replant",
            description: "Replant Lisa."
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performReplant(message.author, null, [
            "_Is being ripped out and thrown away while still alive, watching you plant the next Lisa._"
        ], [
            "_Plants new Lisa on top of the remnants of her ancestors._",
            "_Plants the next generation of Lisa._"
        ]));
    }
}
exports.ReplantCommand = ReplantCommand;
