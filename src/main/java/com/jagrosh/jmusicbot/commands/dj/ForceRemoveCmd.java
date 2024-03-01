/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.util.regex.Matcher;

import static com.jagrosh.jmusicbot.utils.FormatUtil.formatUsername;

/**
 *
 * @author Michaili K.
 */
public class ForceRemoveCmd extends DJCommand
{
    public ForceRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "forceremove";
        this.help = "移除所有由該使用者加入的歌曲";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        if (event.getArgs().isEmpty())
        {
            event.replyError("你必須標記一個成員!");
            return;
        }

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty())
        {
            event.replyError("序列中沒有任何歌曲!");
            return;
        }


        User target = findUser(event.getArgs());

        if (target == null)
        {
            event.replyError("找不到該使用者!");
            return;
        }

        removeAllEntries(target, event);

    }

    private User findUser(String query)
    {
        Matcher userMention = FinderUtil.USER_MENTION.matcher(query);
        Matcher fullRefMatch = FinderUtil.FULL_USER_REF.matcher(query);
        Matcher discordIdMatch = FinderUtil.DISCORD_ID.matcher(query);
        if(userMention.matches() || discordIdMatch.matches())
        {
            String stringId;
            if (userMention.matches())
            {
                stringId = query.replaceAll("[^0-9]", "");
            }
            else
            {
                stringId = query;
            }
            long userId;
            try
            {
                userId = Long.parseLong(stringId);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
            return bot.getJDA().retrieveUserById(userId).complete();
        }
        else if(fullRefMatch.matches())
        {
            String username = fullRefMatch.group(1).toLowerCase() + "#" + fullRefMatch.group(2);

            return bot.getJDA().getUserByTag(username);
        }
        return null;
    }

    private void removeAllEntries(User target, CommandEvent event)
    {
        int count = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue().removeAll(target.getIdLong());
        String discriminator = target.getDiscriminator();

        if (count == 0)
        {
            event.replyWarning("**" + target.getName() + "** 沒有加入任何歌曲!");
        }
        else
        {
            event.replySuccess("成功移除 `" + count + "` 首由 " + formatUsername(target.getName(), discriminator) + " 新增的歌曲");
        }
    }
}