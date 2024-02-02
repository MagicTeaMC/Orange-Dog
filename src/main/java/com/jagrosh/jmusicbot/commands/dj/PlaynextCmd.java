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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlaynextCmd extends DJCommand
{
    private final String loadingEmoji;
    
    public PlaynextCmd(Bot bot)
    {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "playnext";
        this.arguments = "<標題|網址>";
        this.help = "插入一首歌曲到序列的最前面";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
    }
    
    @Override
    public void doCommand(CommandEvent event)
    {
        if(event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty())
        {
            event.replyWarning("請輸入標題或網址!");
            return;
        }
        String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">") 
                ? event.getArgs().substring(1,event.getArgs().length()-1) 
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();
        event.reply(loadingEmoji+" 載入中... `["+args+"]`", m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m,event,false)));
    }
    
    private class ResultHandler implements AudioLoadResultHandler
    {
        private final Message m;
        private final CommandEvent event;
        private final boolean ytsearch;
        
        private ResultHandler(Message m, CommandEvent event, boolean ytsearch)
        {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        private void loadSingle(AudioTrack track) {
            String trackTitle = track.getInfo().title.equals("Unknown title") ? "未知的歌曲" : track.getInfo().title;
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " 這首歌曲 (**" + track.getInfo().title + "**) 比可播放的時間還長： `"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `" + TimeUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrackToFront(new QueuedTrack(track, event.getAuthor()))+1;
            EmbedBuilder mb = new EmbedBuilder();
            mb.setTitle(event.getClient().getSuccess() + " " + trackTitle);
            mb.setColor(Color.pink);
            if(track instanceof YoutubeAudioTrack) mb.setThumbnail("https://img.youtube.com/vi/"+track.getIdentifier()+"/mqdefault.jpg");
            mb.addField("時長", TimeUtil.formatTime(track.getDuration()), true);
            mb.addField("序列", pos == 0 ? "立即播放" : "第 "+pos + " 序列", true);
            mb.addField("歌曲連結", "[點我前往]("+track.getInfo().uri+")", true);
            mb.setFooter(event.getMember().getEffectiveName(), event.getMember().getEffectiveAvatarUrl());

            m.editMessage("♬").queue();
            m.editMessageEmbeds(mb.build()).queue();
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            AudioTrack single;
            if(playlist.getTracks().size()==1 || playlist.isSearchResult())
                single = playlist.getSelectedTrack()==null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            else if (playlist.getSelectedTrack()!=null)
                single = playlist.getSelectedTrack();
            else
                single = playlist.getTracks().get(0);
            loadSingle(single);
        }

        @Override
        public void noMatches()
        {
            if(ytsearch)
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()+" 沒有符合 `"+event.getArgs()+"` 的搜尋結果")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:"+event.getArgs(), new ResultHandler(m,event,true));
        }

        @Override
        public void loadFailed(FriendlyException throwable)
        {
            if(throwable.severity==FriendlyException.Severity.COMMON)
                m.editMessage(event.getClient().getError()+" 載入時發生錯誤: "+throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError()+" 載入歌曲時發生錯誤").queue();
        }
    }
}
