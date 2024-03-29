package com.jagrosh.jmusicbot.commands.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import okhttp3.*;

import java.io.IOException;

public class QkmaoCmd extends Command {
    private final OkHttpClient httpClient;

    public QkmaoCmd(Bot bot) {
        this.name = "qkmao";
        this.help = "透過 Qkmao 縮短網址";
        this.arguments = "<長網址> [短網址名稱]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.httpClient = new OkHttpClient();
    }

    @Override
    public void execute(CommandEvent event) {

        event.getChannel().sendTyping().queue();

        String[] args = event.getArgs().split("\\s+", 2);

        if (args.length < 1) {
            event.replyWarning("正確的使用方法： `>qkmao <長網址> [短網址名稱]`");
            return;
        }

        String link = args[0];
        String customName = args.length > 1 ? args[1] : null;

        if(customName == null) {
            createRandomShortUrl(event, link);
        } else {
            createCustomShortUrl(event, link, customName);
        }
    }

    private void createRandomShortUrl(CommandEvent event, String link) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("link", link);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObject.toString()
        );

        Request request = new Request.Builder()
                .url("https://qkmao.cc/api/v2?random")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        messageLogic(event, request);
    }

    private void createCustomShortUrl(CommandEvent event, String link, String customName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("link", link);
        jsonObject.addProperty("slug", customName);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObject.toString()
        );

        Request request = new Request.Builder()
                .url("https://qkmao.cc/api/v2")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        messageLogic(event, request);
    }

    private void messageLogic(CommandEvent event, Request request) {
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
                String message = jsonObject.get("message").getAsString();

                if(message.equals("successful")) {
                    String shortUrl = jsonObject.get("link").getAsString();
                    event.reply("您的短網址為：<" + shortUrl + ">");
                } else {
                    event.reply("API 回覆：" + message);
                }
            }
        });
    }
}
