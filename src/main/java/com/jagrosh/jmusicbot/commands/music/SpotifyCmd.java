package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyCmd extends MusicCommand {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/api/token";
    private final static String LOAD = "\uD83D\uDCE5"; // 📥
    private final static String CANCEL = "\uD83D\uDEAB"; // 🚫
    private final OrderedMenu.Builder builder;
    private final String loadingEmoji;
    Logger log = LoggerFactory.getLogger(this.name);
    private String accessToken = null;
    private long accessTokenExpirationTime;

    public SpotifyCmd(Bot bot) {

        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "spotify";
        this.arguments = "<網址>";
        this.help = "播放提供的 Spotify 歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;

        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);

        String clientId = bot.getConfig().getSpotifyClientId();
        String clientSecret = bot.getConfig().getSpotifyClientSecret();

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            return;
        }
        accessToken = getAccessToken(clientId, clientSecret);
    }

    public static String extractTrackIdFromUrl(String url) {
        String trackId = null;

        Pattern pattern = Pattern.compile("track/(\\w+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            trackId = matcher.group(1);
        }

        return trackId;
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 沒有指定一個 Spotify 歌曲的網址");
            return;
        }

        String trackUrl = event.getArgs();

        if (System.currentTimeMillis() >= accessTokenExpirationTime) {
            String clientId = bot.getConfig().getSpotifyClientId();
            String clientSecret = bot.getConfig().getSpotifyClientSecret();
            accessToken = getAccessToken(clientId, clientSecret);
        }

        if (!isSpotifyTrackUrl(trackUrl)) {
            event.reply(CANCEL + "本指令目前只支援使用 Spotify 歌曲的網址");
            return;
        }

        String trackId = extractTrackIdFromUrl(trackUrl);
        String endpoint = "https://api.spotify.com/v1/tracks/" + trackId;

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept-Language", "en")
                .GET()
                .uri(URI.create(endpoint))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            String trackName = json.getString("name");
            String albumName = json.getJSONObject("album").getString("name");
            String artistName = json.getJSONArray("artists").getJSONObject(0).getString("name");
            String albumImageUrl = json.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");

            endpoint = "https://api.spotify.com/v1/audio-features/" + trackId;
            request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .uri(URI.create(endpoint))
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            json = new JSONObject(response.body());
            double trackColor = json.getDouble("valence");

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("曲目資訊");
            embed.addField("名稱", trackName, true);
            embed.addField("專輯", albumName, true);
            embed.addField("作者", artistName, true);
            embed.setImage(albumImageUrl);
            embed.setColor(Color.CYAN);

            event.getTextChannel().sendMessageEmbeds(embed.build()).queue();

            event.reply("`[" + trackName + "]`載入中...", m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytmsearch:" + trackName + " " + artistName, new ResultHandler(m, event)));
        } catch (IOException | InterruptedException e) {
            event.reply(CANCEL + e.getMessage());
        }
    }

    public boolean isSpotifyTrackUrl(String url) {
        Pattern pattern = Pattern.compile("https://open\\.spotify\\.com/track/\\w+");
        Matcher matcher = pattern.matcher(url.split("\\?")[0]);

        return matcher.matches();
    }

    private String getAccessToken(String clientId, String clientSecret) {
        String encodedCredentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .uri(URI.create(SPOTIFY_AUTH_URL))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            accessTokenExpirationTime = System.currentTimeMillis() + json.getInt("expires_in") * 1000L;
            return json.getString("access_token");
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;

        private ResultHandler(Message m, CommandEvent event) {
            this.m = m;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + "**" + track.getInfo().title + "**`已經超過可以播放的總時間。"
                        + FormatUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + "**" + track.getInfo().title
                    + "**(`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "並且開始播放"
                    : "至播放清單的第 " + pos + " 序列"))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            builder.setColor(event.getSelfMember().getColor())
                    .setText(FormatUtil.filter(event.getClient().getSuccess() + "搜尋結果："))
                    .setChoices()
                    .setSelection((msg, i) ->
                    {
                        AudioTrack track = playlist.getTracks().get(i - 1);
                        if (bot.getConfig().isTooLong(track)) {
                            event.replyWarning("這首歌曲 (**" + track.getInfo().title + "**) 超過可播放總時間，所以無法播放。: `"
                                    + FormatUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`");
                            return;
                        }
                        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        int pos = handler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;
                        event.replySuccess("加入 **" + FormatUtil.filter(track.getInfo().title)
                                + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "並且開始播放"
                                : "至播放清單的第 " + pos + " 序列"));
                    })
                    .setCancel((msg) -> {
                    })
                    .setUsers(event.getAuthor())
            ;
            for (int i = 0; i < 4 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`[" + FormatUtil.formatTime(track.getDuration()) + "]` [**" + track.getInfo().title + "**](" + track.getInfo().uri + ")");
            }
            builder.build().display(m);
        }

        @Override
        public void noMatches() {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning() + "沒有找到歌曲 `" + event.getArgs() + "`")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {

            if (throwable.severity == FriendlyException.Severity.COMMON)
                m.editMessage(event.getClient().getError() + " 載入時發生錯誤： " + throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError() + " 載入時發生錯誤").queue();
        }
    }
}
