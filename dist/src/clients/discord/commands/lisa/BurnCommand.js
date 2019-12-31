"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const LisaState_1 = require("../../../../lisa/LisaState");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
class BurnCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "burn",
            aliases: ["fire", "killitwithfire"],
            group: "lisa",
            memberName: "burn",
            description: "Burn Lisa (you monster)."
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performKill(message.author, LisaState_1.LisaDeathCause.FIRE, null, [
            "_You hear muffled plant-screams as you set Lisa on fire_",
            "_Lisa looks at you, judging your actions._",
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        ], ["Lisa is already dead!"]));
    }
}
exports.BurnCommand = BurnCommand;
