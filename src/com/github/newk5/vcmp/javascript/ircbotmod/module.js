"use strict";
module.exports = {
    init: function (botConfig, events) {

        IRCWrapper.run(botConfig, events);

        let obj = {
            botName: botConfig.name,
            echo: function (channel, msg) {
                IRCWrapper.echo(botConfig.name, channel, msg);
            }
        }
        return obj;
    },
    color: function(colorName){
        return IRCWrapper.color(colorName);
    }
}