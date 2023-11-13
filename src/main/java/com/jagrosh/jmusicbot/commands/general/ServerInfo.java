package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ServerInfo extends Command {
    public ServerInfo(Bot bot) {
        this.name = "serverinfo";
        this.help = "顯示有關伺服器的資訊";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        String GuildName = event.getGuild().getName();
        String GuildIconURL = event.getGuild().getIconUrl();
        String GuildId = event.getGuild().getId();
        String GuildOwner;
        String GuildCreatedDate = event.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        String GuildRolesCount = String.valueOf(event.getGuild().getRoles().size());
        String GuildMember = String.valueOf(event.getGuild().getMembers().size());
        String GuildCategoryCount = String.valueOf(event.getGuild().getCategories().size());
        String GuildTextChannelCount = String.valueOf(event.getGuild().getTextChannels().size());
        String GuildVoiceChannelCount = String.valueOf(event.getGuild().getVoiceChannels().size());
        String GuildStageChannelCount = String.valueOf(event.getGuild().getStageChannels().size());
        String GuildForumChannelCount = String.valueOf(event.getGuild().getForumChannels().size());
        String GuildLocation = event.getGuild().getLocale().getNativeName();

        if (!Objects.requireNonNull(event.getGuild().getOwner()).getUser().getDiscriminator().equals("0000")) {
            GuildOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getName() + "#" + event.getGuild().getOwner().getUser().getDiscriminator();
        } else {
            GuildOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getName();
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor("伺服器 " + GuildName + " 的資訊", null, GuildIconURL);

        eb.addField("伺服器ID", GuildId, true);
        eb.addField("伺服器主要語言", GuildLocation, true);
        eb.addField("伺服器擁有者", GuildOwner, true);
        eb.addField("成員數", GuildMember, true);
        eb.addField("身分組數", GuildRolesCount, true);
        eb.addField("分類數量", GuildCategoryCount, true);
        eb.addField("文字頻道數量", GuildTextChannelCount, true);
        eb.addField("語音頻道數量", GuildVoiceChannelCount, true);
        eb.addField("舞台頻道數量", GuildStageChannelCount, true);
        eb.addField("論壇頻道數量", GuildForumChannelCount, true);

        eb.setFooter("伺服器創建時間: " + GuildCreatedDate, null);

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
