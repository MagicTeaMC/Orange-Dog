package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfo extends Command {

    public UserInfo() {
        this.name = "userinfo";
        this.help = "顯示有關指定用戶的信息";
        this.arguments = "<用戶>";
        this.guildOnly = true;
    }

    private String translateStatus(OnlineStatus onlineStatus) {
        switch (onlineStatus) {
            case ONLINE:
                return ":white_check_mark: 線上";
            case IDLE:
                return ":yellow_circle: 閒置";
            case DO_NOT_DISTURB:
                return ":red_circle: 請勿打擾";
            case OFFLINE:
                return ":black_circle: 離線";
            default:
                return ":x: 發生錯誤";
        }
    }

    @Override
    public void execute(CommandEvent event) {
        Member memb;

        if (!event.getArgs().isEmpty()) {
            try {
                if (!event.getMessage().getMentions().getMembers().isEmpty()) {
                    memb = event.getMessage().getMentions().getMembers().get(0);
                } else {
                    List<Member> foundMembers = FinderUtil.findMembers(event.getArgs(), event.getGuild());
                    if (!foundMembers.isEmpty()) {
                        memb = foundMembers.get(0);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            } catch (Exception e) {
                event.reply("未找到用戶 \"" + event.getArgs() + "\" 。");
                return;
            }
        } else {
            memb = event.getMember();
        }

        EmbedBuilder eb = new EmbedBuilder().setColor(memb.getColor());
        String NAME = memb.getEffectiveName();
        String TAG;
        if (!memb.getUser().getDiscriminator().equals("0000")){
            TAG = "#" + memb.getUser().getDiscriminator();
        } else {
            TAG = null;
        }
        String GUILD_JOIN_DATE = memb.getTimeJoined().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String DISCORD_JOINED_DATE = memb.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String ID = memb.getUser().getId();
        String STATUS = translateStatus(memb.getOnlineStatus());
        String ROLES;
        String GAME;
        List<String> games = memb.getActivities().stream()
                .filter(activity -> activity.getType() == Activity.ActivityType.PLAYING)
                .map(Activity::getName)
                .collect(Collectors.toList());
        GAME = games.isEmpty() ? "-/-" : String.join(", ", games);

        String AVATAR = memb.getUser().getAvatarUrl();

        StringBuilder ROLESBuilder = new StringBuilder();
        for (Role r : memb.getRoles()) {
            ROLESBuilder.append(r.getName()).append(", ");
        }
        ROLES = ROLESBuilder.toString();
        if (!ROLES.isEmpty())
            ROLES = ROLES.substring(0, ROLES.length() - 2);
        else
            ROLES = "伺服器中沒有身分組";

        if (AVATAR == null) {
            AVATAR = "沒有頭像";
        }
        if (TAG!=null) {
            eb.setAuthor(memb.getUser().getName() + TAG + " 的資訊", null, null)
                    .addField(":pencil2: 暱稱", "**" + NAME + "**", true)
                    .addField(":link: Discord 標籤", "**" + TAG + "**", true)
                    .addField(":1234: 用戶 ID", "**" + ID + "**", true)
                    .addBlankField(false)
                    .addField(":signal_strength: 當前狀態", "**" + STATUS + "**", true)
                    .addField(":video_game: 正在遊玩的遊戲", "**" + GAME + "**", true)
                    .addField(":tools: 身分組", "**" + ROLES + "**", true)
                    .addBlankField(false)
                    .addField(":inbox_tray: 加入伺服器時間", "**" + GUILD_JOIN_DATE + "**", true)
                    .addField(":beginner: 帳戶創建時間", "**" + DISCORD_JOINED_DATE + "**", true)
                    .addBlankField(false)
                    .addField(":frame_photo: 頭像 URL", AVATAR, false);
            if (!AVATAR.equals("沒有頭像")) {
                eb.setAuthor(memb.getUser().getName() + TAG + " 的資訊", null, AVATAR);
            }
        } else {
            eb.setAuthor(memb.getUser().getName() + " 的資訊", null, null)
                    .addField(":pencil2: 暱稱", "**" + NAME + "**", true)
                    .addField(":1234: 用戶 ID", "**" + ID + "**", true)
                    .addBlankField(false)
                    .addField(":signal_strength: 當前狀態", "**" + STATUS + "**", true)
                    .addField(":video_game: 正在遊玩的遊戲", "**" + GAME + "**", true)
                    .addField(":tools: 身分組", "**" + ROLES + "**", true)
                    .addBlankField(false)
                    .addField(":inbox_tray: 加入伺服器時間", "**" + GUILD_JOIN_DATE + "**", true)
                    .addField(":beginner: 帳戶創建時間", "**" + DISCORD_JOINED_DATE + "**", true)
                    .addBlankField(false)
                    .addField(":frame_photo: 頭像 URL", AVATAR, false);
            if (!AVATAR.equals("沒有頭像")) {
                eb.setAuthor(memb.getUser().getName() + " 的資訊", null, AVATAR);
            }
        }

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
