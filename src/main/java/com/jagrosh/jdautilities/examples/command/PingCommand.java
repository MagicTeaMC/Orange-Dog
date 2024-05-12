/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.temporal.ChronoUnit;

/**
 * @author John Grosh (jagrosh)
 */
@CommandInfo(name = {"Ping", "Pong"}, description = "查看機器人延遲")
@Author("John Grosh (jagrosh)")
public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "查看機器人延遲";
        this.guildOnly = false;
        this.aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.CYAN);
        embed.setTitle("機器人延遲");
        embed.addField("延遲", "...", false);
        event.getChannel().sendMessageEmbeds(embed.build()).queue(response -> {
            long ping = event.getMessage().getTimeCreated().until(response.getTimeCreated(), ChronoUnit.MILLIS);
            embed.clearFields();
            embed.addField("延遲", ping + "毫秒", false);
            embed.addField("Websocket延遲", event.getJDA().getGatewayPing() + "毫秒", false);
            response.editMessageEmbeds(embed.build()).queue();
        });
    }

}
