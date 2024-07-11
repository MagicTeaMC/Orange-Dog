package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

public class DownloadCmd extends MusicCommand {

    public DownloadCmd(Bot bot) {
        super(bot);
        this.name = "download";
        this.help = "下載 YouTube 影片";
        this.arguments = "<URL|影片名稱>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 使用方法：`>download <URL|影片名稱>`");
            return;
        }

        event.getChannel().sendTyping().queue();
        String query = event.getArgs();

        searchSong(query, event);
    }

    private void searchSong(String query, CommandEvent event) {
        Bot bot = this.bot;
        bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:" + query, new ResultHandler(event));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final CommandEvent event;

        private ResultHandler(CommandEvent event) {
            this.event = event;
        }

        private void loadSingle(AudioTrack track) {
            String videoId = track.getInfo().identifier;
            String title = track.getInfo().title;

            event.reply("找到影片： `" + title + "`\n[點此下載](https://aweirddev-yt.hf.space/mp4?q=" + videoId + ") MP4 檔案");
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                AudioTrack single = playlist.getSelectedTrack() == null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                loadSingle(single);
            } else {
                event.reply("本指令只支援單個影片下載");
            }
        }

        @Override
        public void noMatches() {
            event.reply("找不到符合的 YouTube 影片。");
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            event.reply("載入影片時出現錯誤: " + throwable.getMessage());
        }
    }
}