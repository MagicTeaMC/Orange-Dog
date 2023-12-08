/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.entities.Guild;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class LeaveServerCmd extends OwnerCommand {
    private final Bot bot;

    public LeaveServerCmd(Bot bot) {
        this.bot = bot;
        this.name = "leaveserver";
        this.help = "離開特定的伺服器";
        this.arguments = "<伺服器 ID>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("請輸入伺服器 ID");
            return;
        }
        try {
            Long.parseLong(event.getArgs());
        } catch (Exception e) {
            event.replyError("請輸入有效的伺服器 ID");
            return;
        }
        final Guild server = event.getJDA().getGuildById(event.getArgs());
        if (server == null) {
            event.replyError("未知的伺服器");
            return;
        }
        server.leave().complete();
        event.reply("成功離開伺服器 \"" + server.getName() + "\"(`" + server.getId() + "`)");
    }
}
