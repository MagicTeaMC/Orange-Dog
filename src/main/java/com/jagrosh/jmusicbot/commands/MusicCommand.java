/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

import static com.jagrosh.jmusicbot.utils.FormatUtil.formatUsername;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends Command {
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;

    public MusicCommand(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("音樂");
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        String authorId = event.getAuthor().getId();
        boolean authorCannotUseCommands = settings.getBlacklistedUsers().contains(authorId);
        if (authorCannotUseCommands) {
            String authorTag = event.getAuthor().getAsTag();
            event.replyError(formatUsername(event.getAuthor().getName(), event.getAuthor().getDiscriminator()) + " 在黑名單中，所以無法使用此指令！");
            return;
        }
        TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if (tchannel != null && !event.getTextChannel().equals(tchannel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) {
            }
            event.replyInDm(event.getClient().getError() + " 你只能在 " + tchannel.getAsMention() + " 中使用這個指令！");
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " 必須正在播放音樂！");
            return;
        }
        if (beListening) {
            AudioChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null)
                current = settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inAudioChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.replyError("你必須在 " + (current == null ? "語音頻道中" : current.getAsMention()) + " 才能使用這個指令！");
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyError("你不能在閒置頻道中使用這個指令！");
                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + " 我無法加入 " + userState.getChannel().getAsMention() + " ！");
                    return;
                }
            }
        }

        doCommand(event);
    }

    public abstract void doCommand(CommandEvent event);
}
