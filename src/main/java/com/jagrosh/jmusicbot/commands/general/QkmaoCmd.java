package com.jagrosh.jmusicbot.commands.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import okhttp3.*;

import java.io.IOException;

public class QkmaoCmd extends MusicCommand {
    private final OkHttpClient httpClient;

    public QkmaoCmd(Bot bot) {
        super(bot);
        this.name = "qkmao";
        this.help = "透過 Qkmao 縮短網址";
        this.arguments = "<長網址> [短網址名稱]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.httpClient = new OkHttpClient();
    }

    @Override
    public void doCommand(CommandEvent event) {

        event.getChannel().sendTyping().queue();

        String link = event.getArgs();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                "{\"link\":\"" + link + "\"}"
        );

        Request request = new Request.Builder()
                .url("https://qkmao.cc/api/v2?random")
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
                String shortUrl = jsonObject.get("url").getAsString();
                String code = jsonObject.get("message").getAsString();

                if(code.equals("successful")) {
                    event.reply("您的短網址為：<" + shortUrl + ">");
                } else if(code.equals("used")) {
                    event.reply("此你提供的短網址名稱無法使用");
                } else {
                    event.reply("這個不該發生...請考慮回報給我們");
                }
            }
        });
    }
}
