package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class DownloadCmd extends MusicCommand {
    private final OkHttpClient httpClient;

    public DownloadCmd(Bot bot) {
        super(bot);
        this.name = "download";
        this.help = "下載 YouTube 影片";
        this.arguments = "<URL|影片名稱>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.httpClient = new OkHttpClient();
    }

    @Override
    public void doCommand(CommandEvent event) {

        event.getChannel().sendTyping().queue();

        String query = event.getArgs();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                "{\"query\":\"" + query + "\"}"
        );

        Request request = new Request.Builder()
                .url("https://qkmao.cc/api/v2/youtube")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                event.reply("發生錯誤 " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    event.reply("API 發生錯誤 " + response.message());
                    return;
                }

                String jsonData = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                String downloadUrl = jsonObject.get("url").getAsString();
                String videoTitle = jsonObject.get("title").getAsString();
                event.reply("[點此下載](" + downloadUrl + ") `" + videoTitle + "` 的音檔");
            }
        });
    }
}