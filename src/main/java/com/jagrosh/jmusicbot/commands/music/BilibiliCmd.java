package com.jagrosh.jmusicbot.commands.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BilibiliCmd extends MusicCommand {
    private final OkHttpClient httpClient;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";

    public BilibiliCmd(Bot bot) {
        super(bot);
        this.name = "bilibili";
        this.help = "播放來自 BiliBili 的音樂";
        this.arguments = "<URL|影片名稱>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.httpClient = new OkHttpClient();
    }

    @Override
    public void doCommand(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String query = event.getArgs();

        String audioUrl = getAudioUrl(query);
        if (audioUrl != null) {
            event.reply("[點此下載](" + audioUrl + ") 的音檔");
        } else {
            event.reply("無法獲取音頻URL，請確認BiliBili視頻ID是否正確。");
        }
    }

    public String getAudioUrl(String bvid) {
        try {
            String url = "https://www.bilibili.com/video/" + bvid;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Referer", url);

            InputStream in = conn.getInputStream();
            String response = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));

            String jsonData = getJsonData(response);

            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            return jsonObject.getAsJsonObject("data")
                    .getAsJsonObject("dash")
                    .getAsJsonArray("audio")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("backupUrl")
                    .get(0).getAsString();
        } catch (Exception e) {
            System.err.println("bvid:" + bvid + " 音频URL获取失败，已经跳过");
            e.printStackTrace();
            return null;
        }
    }

    private String getJsonData(String response) {
        Pattern pattern = Pattern.compile("<script>window.__playinfo__=(.*?)</script>");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Unable to find JSON data in response.");
        }
    }
}