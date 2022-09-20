package tv.quaint.discordmodule.discord.messaging;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesSentStat;

import java.util.Optional;

public class DiscordMessenger {
    public static void incrementMessageCountOut() {
        DiscordModule.getBotStats().getMessagesSentStat().increment();
    }

    public static void incrementMessageCountIn() {
        DiscordModule.getBotStats().getMessagesRecievedStat().increment();
    }

    public static void incrementMessageCountInBots() {
        DiscordModule.getBotStats().getBotMessagesRecievedStat().increment();
    }

    public static void sendMessage(long channelId, String message, StreamlineUser on, boolean format) {
        if (format) message = ModuleUtils.replaceAllPlayerBungee(on, message);

        Optional<TextChannel> optionalChannel = DiscordHandler.getTextChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to TextChannel with ID of '" + channelId + "', but failed.");
            return;
        }
        TextChannel channel = optionalChannel.get();

        MessageBuilder builder = new MessageBuilder(); // https://javacord.org/wiki/basic-tutorials/message-builder.html
        builder.append(message);
        builder.send(channel);

        incrementMessageCountOut();
    }

    public static void sendMessage(long channelId, String message, StreamlineUser user) {
        sendMessage(channelId, message, user, true);
    }

    public static void sendMessage(long channelId, String message, boolean format) {
        sendMessage(channelId, message, ModuleUtils.getConsole(), format);
    }

    public static void sendMessage(long channelId, String message) {
        sendMessage(channelId, message, ModuleUtils.getConsole(), true);
    }

    public static void sendSimpleEmbed(long channelId, String message, String title, StreamlineUser on, boolean formatMessage, boolean formatTitle, String authorName, String authorUrl, String iconUrl) {
        if (formatMessage) message = ModuleUtils.stripColor(ModuleUtils.replaceAllPlayerBungee(on, message));
        if (formatTitle) title = ModuleUtils.stripColor(ModuleUtils.replaceAllPlayerBungee(on, title));

        Optional<TextChannel> optionalChannel = DiscordHandler.getTextChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to TextChannel with ID of '" + channelId + "', but failed.");
            return;
        }
        TextChannel channel = optionalChannel.get();

        MessageBuilder builder = new MessageBuilder(); // https://javacord.org/wiki/basic-tutorials/message-builder.html
        builder.append(new EmbedBuilder()
                .setAuthor(ModuleUtils.stripColor(authorName), authorUrl, iconUrl)
                .setTitle(title)
                .setDescription(message)
        );
        builder.send(channel);
    }

    public static void sendSimpleEmbed(long channelId, String message, String title, StreamlineUser on, boolean formatMessage, boolean formatTitle) {
        sendSimpleEmbed(channelId, message, title, on, formatMessage, formatTitle, on.get);
    }
}
