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
package com.jagrosh.jmusicbot.commands.music;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.List;

public class LyricsCmd extends MusicCommand {
    public LyricsCmd(Bot bot) {
        super(bot);
        this.name = "lyrics";
        this.arguments = "[歌曲名稱]";
        this.help = "顯示歌曲歌詞";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (sendingHandler.isMusicPlaying(event.getJDA())) {
                AudioTrack track = sendingHandler.getPlayer().getPlayingTrack();
                handleLyrics(track, event);
            } else {
                event.replyError("必須要有音樂正在播放才能使用這個指令!");
            }
        } else {
            String query = event.getArgs();
            searchSong(query, event);
        }
    }

    private void searchSong(String query, CommandEvent event) {
        bot.getPlayerManager().loadItemOrdered(event.getGuild(), "spsearch:" + query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                handleLyrics(track, event);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : playlist.getTracks().get(0);
                handleLyrics(track, event);
            }

            @Override
            public void noMatches() {
                event.replyError("找不到符合的歌曲: " + query);
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                event.replyError("載入歌曲時發生錯誤: " + throwable.getMessage());
            }
        });
    }

    private void handleLyrics(AudioTrack track, CommandEvent event) {
        event.getChannel().sendTyping().queue();

        AudioLyrics audioLyrics = bot.getPlayerManager().getLyricsManager().loadLyrics(track, true);
        if (audioLyrics == null) {
            event.replyError("無法找到 `" + track.getInfo().title + "` 的歌詞!");
            return;
        }

        List<AudioLyrics.Line> lines = audioLyrics.getLines();
        if (lines == null || lines.isEmpty()) {
            event.replyError("無法解析 `" + track.getInfo().title + "` 的歌詞!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (AudioLyrics.Line line : lines) {
            sb.append(line.getLine()).append("\n");
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(track.getInfo().author)
                .setColor(event.getSelfMember().getColor())
                .setTitle(track.getInfo().title, track.getInfo().uri);

        if (sb.length() > 15000) {
            event.replyWarning("找到了 `" + track.getInfo().title + "` 的歌詞，但是看起來歌詞是錯誤的");
        } else if (sb.length() > 2000) {
            String content = sb.toString().trim();
            while (content.length() > 2000) {
                int index = content.lastIndexOf("\n\n", 2000);
                if (index == -1)
                    index = content.lastIndexOf("\n", 2000);
                if (index == -1)
                    index = content.lastIndexOf(" ", 2000);
                if (index == -1)
                    index = 2000;
                event.reply(eb.setDescription(content.substring(0, index).trim()).build());
                content = content.substring(index).trim();
                eb.setAuthor(null).setTitle(null, null);
            }
            event.reply(eb.setDescription(content).build());
        } else {
            event.reply(eb.setDescription(sb.toString()).build());
        }
    }
}