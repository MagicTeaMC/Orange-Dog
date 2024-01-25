/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.jagrosh.jmusicbot;

import ch.qos.logback.classic.Level;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.*;
import com.jagrosh.jmusicbot.commands.admin.*;
import com.jagrosh.jmusicbot.commands.dj.*;
import com.jagrosh.jmusicbot.commands.general.*;
import com.jagrosh.jmusicbot.commands.general.AboutCommand;
import com.jagrosh.jmusicbot.commands.music.*;
import com.jagrosh.jmusicbot.commands.owner.*;
import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import java.awt.Color;
import java.util.Arrays;

import com.jagrosh.jmusicbot.utils.ShutdownListener;
import me.scarsz.jdaappender.ChannelLoggingHandler;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class JMusicBot
{
    public final static String PLAY_EMOJI  = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI  = "\u23F9"; // ⏹
    public final static Permission[] RECOMMENDED_PERMS = {Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI,
            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
    public final static GatewayIntent[] INTENTS = {GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.SCHEDULED_EVENTS, GatewayIntent.GUILD_MEMBERS};
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ShutdownListener listener = new ShutdownListener();
        listener.start();
        String[] pattern = {
                "     ____  _____  ____        _   ",
                "    / __ \\|  __ \\|  _ \\      | |  ",
                "   | |  | | |  | | |_) | ___ | |_ ",
                "   | |  | | |  | |  _ < / _ \\| __|",
                "   | |__| | |__| | |_) | (_) | |_ ",
                "    \\____/|_____/|____/ \\___/ \\__|",
                "   By MagicTeaMC(Maoyue)",
                "   GitHub: https://github.com/MagicTeaMC/Orange-Dog",
                "   Discord: https://discord.gg/uQ4UXANnP2"
        };

        for (String line : pattern) {
            System.out.println(line);
        }
        System.out.println
                (
                "\n" +
                "╔════════════════════════════════════════════════════════════════╗\n" +
                "║                          _ooOoo_                               ║\n" +
                "║                         o8888888o                              ║ \n" +
                "║                         88\" . \"88                              ║\n" +
                "║                         (| ^_^ |)                              ║\n" +
                "║                         O\\  =  /O                              ║\n" +
                "║                      ____/`---'\\____                           ║\n" +
                "║                    .'  \\|     |//   '.                         ║\n" +
                "║                   /  \\|||  :  |||//   \\                        ║\n" +
                "║                  /  _||||| -:- |||||-  \\                       ║\n" +
                "║                  |   | \\\\  -  ///  |   |                       ║\n" +
                "║                  | \\_|  ''\\---/''  |   |                       ║\n" +
                "║                  \\  .-\\__  `-`  ___/-. /                       ║\n" +
                "║                ___`. .'  /--.--\\  `. . ___                     ║\n" +
                "║              .\"\"\"<  `.___\\_<|>_/___.`  >\"\"\".                   ║\n" +
                "║            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 ║\n" +
                "║            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 ║\n" +
                "║      ========`-.____`-.___\\_____/___.-`____.-'========         ║\n" +
                "║                           `=---='                              ║ \n" +
                "║      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        ║\n" +
                "║            佛祖保佑       永不崩潰     永不抱錯                ║\n" +
                "╚════════════════════════════════════════════════════════════════╝\n"
                );
        // startup log
        Logger log = LoggerFactory.getLogger("啟動");

        // create prompt to handle startup
        Prompt prompt = new Prompt("機器人", "正在切換到無視窗模式，您可以使用 -Dnogui=false 來關閉這個功能");


        // check for valid java version
        if(!System.getProperty("java.vm.name").contains("64"))
            prompt.alert(Prompt.Level.WARNING, "Java版本", "您的Java可能無法跟此程式相容，請使用64位元的Java");

        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if(!config.isValid())
            return;

        // set log level from config
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(
                Level.toLevel(config.getLogLevel(), Level.INFO));

        // set up the listener
        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);

        AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(),
                "",
                new String[]{"高品質的音樂", "FairQueue™ 技術"},
                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶

        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwnerId()))
                .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                .setHelpWord(config.getHelp())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .addCommands(aboutCommand,
                        new ServerInfo(bot),
                        new PingCommand(),
                        new SettingsCmd(bot),
                        new UserInfo(),

                        new DownloadCmd(bot),
                        new LyricsCmd(bot),
                        new NowplayingCmd(bot),
                        new PlayCmd(bot),
                        new PlaylistsCmd(bot),
                        new QueueCmd(bot),
                        new RemoveCmd(bot),
                        new SearchCmd(bot),
                        new SCSearchCmd(bot),
                        new ShuffleCmd(bot),
                        new SkipCmd(bot),
                        new SeekCmd(bot),
                        new skipSegCmd(bot),
                        new SpotifyCmd(bot),

                        new ForceRemoveCmd(bot),
                        new ForceskipCmd(bot),
                        new MoveTrackCmd(bot),
                        new PauseCmd(bot),
                        new PlaynextCmd(bot),
                        new RepeatCmd(bot),
                        new ShuffleAllCmd(bot),
                        new SkiptoCmd(bot),
                        new StopCmd(bot),
                        new VolumeCmd(bot),

                        new BlacklistUserCmd(bot),
                        new PrefixCmd(bot),
                        new SetdjCmd(bot),
                        new SettcCmd(bot),
                        new SetvcCmd(bot),

                        new AutoplaylistCmd(bot),
                        new DebugCmd(bot),
                        new PlaylistCmd(bot),
                        new SetavatarCmd(bot),
                        new SetgameCmd(bot),
                        new SetnameCmd(bot),
                        new SetstatusCmd(bot),
                        new ShutdownCmd(bot),
                        new LeaveServerCmd(bot),
                        new ServersCmd(bot)
                );
        if(config.useEval())
            cb.addCommand(new EvalCmd(bot));
        boolean nogame = false;
        if(config.getStatus()!=OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if(config.getGame()==null)
            cb.useDefaultGame();
        else if(config.getGame().getName().equalsIgnoreCase("none"))
        {
            cb.setActivity(null);
            nogame = true;
        }
        else
            cb.setActivity(config.getGame());

        log.info("成功從 " + config.getConfigLocation() + " 讀取配置");

        // attempt to log in and start
        try
        {
            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS)
                    .setActivity(nogame ? null : Activity.playing("載入中..."))
                    .setStatus(config.getStatus()==OnlineStatus.INVISIBLE || config.getStatus()==OnlineStatus.OFFLINE
                            ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot))
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);

            new ChannelLoggingHandler(() -> jda.getTextChannelById(1195875816593174609L))
                    .attach() // attach to SLF4J JDK logging if present, else Log4j if present, else standard out/err
                    .schedule(); // schedule handler to flush output asynchronously every 1.5 seconds
        }
        catch (InvalidTokenException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "機器人", ex + "\n請確定您更改了正確的配置文件，並且放置了正確的機器人Token (不是secret)"
                    + "\n配置文件路徑: " + config.getConfigLocation());
            System.exit(1);
        }
        catch(IllegalArgumentException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "機器人", "部分配置文件語法錯誤: "
                    + ex + "\n配置文件路徑: " + config.getConfigLocation());
            System.exit(1);
        }
        catch(ErrorResponseException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "JMusicBot", ex + "\nInvalid reponse returned when "
                    + "attempting to connect, please make sure you're connected to the internet");
            System.exit(1);
        }
    }
}