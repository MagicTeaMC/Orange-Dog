package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

@CommandInfo(
        name = {"About"},
        description = "顯示關於機器人的資訊"
)
@Author("John Grosh (jagrosh)")
public class AboutCommand extends Command {
    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;

    public AboutCommand(Color color, String description, String[] features, Permission... perms) {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "顯示關於機器人的資訊";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    public void setIsAuthor(boolean value) {
        this.IS_AUTHOR = value;
    }

    public void setReplacementCharacter(String value) {
        this.REPLACEMENT_ICON = value;
    }

    protected void execute(CommandEvent event) {
        if (this.oauthLink == null) {
            try {
                ApplicationInfo info = (ApplicationInfo)event.getJDA().retrieveApplicationInfo().complete();
                this.oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, this.perms) : "";
            } catch (Exception var12) {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("Could not generate invite link ", var12);
                this.oauthLink = "";
            }
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : this.color);
        builder.setAuthor(event.getSelfUser().getName() + " 的個人資料！", null, event.getSelfUser().getAvatarUrl());
        boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
        boolean inv = !this.oauthLink.isEmpty();
        String invline = "\n" + (join ? "Join my server [`here`](" + event.getClient().getServerInvite() + ")" : (inv ? "請 " : "")) + (inv ? (join ? ", 或 " : "") + "[`邀請`](" + this.oauthLink + ") 我到你的伺服器" : "") + "!";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">" : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        StringBuilder descr = (new StringBuilder()).append("你好！ 我是 **").append(event.getSelfUser().getName()).append("**。 ").append(this.IS_AUTHOR ? "使用Java寫成" : "我的擁有者是").append(" **").append(author).append("** ，本機器人依賴於 [JDA Utilities](https://github.com/JDA-Applications/JDA-Utilities) (").append(JDAUtilitiesInfo.VERSION).append(") 以及 [JDA wrapper](https://github.com/DV8FromTheWorld/JDA) (").append(JDAInfo.VERSION).append(")\n\n請輸入 `").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("` 來查看我的指令!").append(join || inv ? invline : "");
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ");

        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == null)
        {
            builder.addField("狀態", event.getJDA().getGuilds().size() + " 個伺服器\n1 個分片", true);
            builder.addField("使用者", event.getJDA().getUsers().size() + " 個用戶\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            builder.addField("頻道", event.getJDA().getTextChannels().size() + " 個文字頻道\n" + event.getJDA().getVoiceChannels().size() + " 個語音頻道", true);
        }
        else
        {
            builder.addField("狀態", (event.getClient()).getTotalGuilds() + " 個伺服器\n於分片 " + (event.getJDA().getShardInfo().getShardId() + 1)
                    + "/" + event.getJDA().getShardInfo().getShardTotal() + "上", true);
            builder.addField("此分片有", event.getJDA().getUsers().size() + " 個使用者\n" + event.getJDA().getGuilds().size() + " 個伺服器", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " 個文字頻道\n" + event.getJDA().getVoiceChannels().size() + " 個語音頻道", true);
        }
        builder.setFooter("最後一次重新啟動", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
}
