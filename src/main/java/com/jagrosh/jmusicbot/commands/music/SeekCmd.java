package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;

import java.util.regex.Pattern;

public class SeekCmd extends MusicCommand {
    public SeekCmd(Bot bot) {
        super(bot);
        this.name = "seek";
        this.help = "跳轉至歌曲的特定時間";
        this.arguments = "<HH:MM:SS>|<MM:SS>|<SS>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().getPlayingTrack().isSeekable()) {
            AudioTrack currentTrack = handler.getPlayer().getPlayingTrack();
            Settings settings = event.getClient().getSettingsFor(event.getGuild());

            if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                if (!event.getMember().getRoles().contains(settings.getRole(event.getGuild()))) {
                    if (currentTrack.getUserData(Long.class) != event.getAuthor().getIdLong()) {
                        event.replyError("只有點播 **" + currentTrack.getInfo().title + "** 的用戶才能跳轉歌曲時間");
                        return;
                    }
                }
            }

            String args = event.getArgs();
            long track_duration = handler.getPlayer().getPlayingTrack().getDuration();
            int seek_milliseconds = 0;
            int seconds = 0;
            int minutes = 0;
            int hours = 0;

            if (Pattern.matches("^([0-9]{1,2}):([0-5]\\d):([0-5]\\d)$", args)) {
                hours = Integer.parseInt(args.split(":")[0]);
                minutes = Integer.parseInt(args.split(":")[1]);
                seconds = Integer.parseInt(args.split(":")[2]);
            } else if (Pattern.matches("^([0-9]{1,2}):([0-5]\\d)$", args)) {
                minutes = Integer.parseInt(args.split(":")[0]);
                seconds = Integer.parseInt(args.split(":")[1]);
            } else if (Pattern.matches("^([0-9]{1,2})$", args)) {
                seconds = Integer.parseInt(args);
            } else {
                event.replyError("時間格式錯誤！ 正確格式：`<HH:MM:SS>|<MM:SS>|<SS>`");
                return;
            }

            minutes += seconds / 60;
            seconds = seconds % 60;

            hours += minutes / 60;
            minutes = minutes % 60;

            seek_milliseconds += hours * 3600000 + minutes * 60000 + seconds * 1000;
            if (seek_milliseconds <= track_duration) {
                handler.getPlayer().getPlayingTrack().setPosition(seek_milliseconds);
                String responseTime = hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                        : String.format("%02d:%02d", minutes, seconds);
                event.replySuccess("成功跳轉到 `" + responseTime + "`！");
            } else {
                event.replyError("目前歌曲沒有所指定的時間！");
            }
        } else {
            event.replyError("此歌曲無法跳轉。");
        }
    }
}
