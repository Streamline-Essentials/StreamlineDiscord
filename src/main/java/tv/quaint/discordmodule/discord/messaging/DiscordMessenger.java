package tv.quaint.discordmodule.discord.messaging;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesSentStat;

import java.util.Optional;

public class DiscordMessenger {
    public static void incrementMessageCountIn() {
        ((MessagesRecievedStat) DiscordModule.getBotStats().getLoadedStats().get(MessagesRecievedStat.class.getSimpleName())).increment();
    }

    public static void incrementMessageCountOut() {
        ((MessagesSentStat) DiscordModule.getBotStats().getLoadedStats().get(MessagesSentStat.class.getSimpleName())).increment();
    }

    public static void incrementMessageCountOutBots() {
        ((BotMessagesRecievedStat) DiscordModule.getBotStats().getLoadedStats().get(BotMessagesRecievedStat.class.getSimpleName())).increment();
    }

    public static void sendMessage(long channelId, String message, StreamlineUser on, boolean format) {
        if (format) message = ModuleUtils.replaceAllPlayerBungee(on, message);

        Optional<ServerChannel> optionalChannel = DiscordHandler.getServerChannelById(channelId);
        if (optionalChannel.isEmpty()) {
            DiscordModule.getInstance().logWarning("Tried to send a message to channel with ID of '" + channelId + "', but failed.");
            return;
        }
        ServerChannel channel = optionalChannel.get();
        if (! channel.getType().equals(ChannelType.SERVER_NEWS_CHANNEL) || ! channel.getType().equals(ChannelType.SERVER_PRIVATE_THREAD)
                || ! channel.getType().equals(ChannelType.SERVER_PUBLIC_THREAD) || ! channel.getType().equals(ChannelType.SERVER_TEXT_CHANNEL)) return;
        channel.getServer().message
    }
}
