"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const discord_js_commando_1 = require("discord.js-commando");
const IMAGE_LINK = "http://static.tumblr.com/df323b732955715fe3fb5a506999afc7/" +
    "rflrqqy/H9Cnsyji6/tumblr_static_88pgfgk82y4ok80ckowwwwow4.jpg";
const ABOUT_MESSAGE = `Hello!
I am Lisa, an indoor plant inspired by Lisa from 'Life is Strange'.
<http://dontnodentertainment.wikia.com/wiki/Lisa_the_Plant>
----------
For more information, use \`$help\` or go to <https://github.com/FelixRilling/lisa-bot>.
If you have questions or want to report a bug, send me a mail at lisa-bot@rilling.dev.`;
class AboutCommand extends discord_js_commando_1.Command {
    constructor(client) {
        super(client, {
            name: "about",
            aliases: ["why", "info"],
            group: "util",
            memberName: "about",
            description: "Shows info about the bot."
        });
    }
    run(message) {
        return message.say(ABOUT_MESSAGE, { files: [IMAGE_LINK] });
    }
}
exports.AboutCommand = AboutCommand;
