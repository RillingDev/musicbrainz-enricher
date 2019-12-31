"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const chevron_1 = require("../../../../chevron");
const DiscordCommandController_1 = require("../../controller/DiscordCommandController");
const NIKLAS_ID = ["178470784984023040"];
class NiklasCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "niklas",
            aliases: [],
            group: "lisa",
            memberName: "niklas",
            description: "^w^",
            hidden: true
        });
        this.lisaDiscordCommandController = chevron_1.chevron.getInjectableInstance(DiscordCommandController_1.DiscordCommandController);
    }
    run(message) {
        return message.say(this.lisaDiscordCommandController.performAction(message.author, 0, 40, NIKLAS_ID, ["_tight huggu_"], ["OwO whats this? a dead Lisa..."], ["You're not a niklas uwu"]));
    }
}
exports.NiklasCommand = NiklasCommand;
