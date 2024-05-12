/*
 * Copyright 2020 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * @author Whew., Inc.
 */
public class SeekCmd extends MusicCommand {
    public SeekCmd(Bot bot) {
        super(bot);
        this.name = "seek";
        this.help = "跳轉至歌曲的特定時間";
        this.arguments = "[+ | -] <HH:MM:SS | MM:SS | SS>|<0h0m0s | 0m0s | 0s>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AudioTrack playingTrack = handler.getPlayer().getPlayingTrack();
        if (!playingTrack.isSeekable()) {
            event.replyError("此歌曲無法跳轉");
            return;
        }
        if (!DJCommand.checkDJPermission(event) && playingTrack.getUserData(Long.class) != event.getAuthor().getIdLong()) {
            event.replyError("只有點播 **" + playingTrack.getInfo().title + "** 的用戶才能跳轉歌曲時間");
            return;
        }
        String args = event.getArgs();
        TimeUtil.SeekTime seekTime = TimeUtil.parseTime(args);
        if (seekTime == null) {
            event.replyError("時間格式錯誤！ 正確格式：`1:02:23` `+1:10` `-90`, `1h10m`, `+90s`");
            return;
        }

        long currentPosition = playingTrack.getPosition();
        long trackDuration = playingTrack.getDuration();
        long seekMilliseconds = seekTime.relative ? currentPosition + seekTime.milliseconds : seekTime.milliseconds;
        if (seekMilliseconds > trackDuration) {
            event.replyError("無法跳轉至 `" + TimeUtil.formatTime(seekMilliseconds) + "` 因為現在的歌曲長度只有 `" + TimeUtil.formatTime(trackDuration));
        } else {
            try {
                playingTrack.setPosition(seekMilliseconds);
            } catch (Exception e) {
                event.replyError("跳轉時發生錯誤");
                e.printStackTrace();
                return;
            }
        }
        event.replySuccess("成功跳轉至 `" + TimeUtil.formatTime(playingTrack.getPosition()) + "/" + TimeUtil.formatTime(playingTrack.getDuration()) + "`!");
    }
}
