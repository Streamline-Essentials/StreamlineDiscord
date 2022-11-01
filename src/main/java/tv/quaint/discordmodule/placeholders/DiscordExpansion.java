package tv.quaint.discordmodule.placeholders;

import net.streamline.api.SLAPI;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscordExpansion extends RATExpansion {
    public DiscordExpansion() {
        super("discord", "Quaint", "0.0.1");
    }

    @Override
    public String onLogic(String s) {
        if (s.equals("bot_ping")) {
            double millis = DiscordHandler.safeDiscordAPI().getGatewayPing();
            String r = String.valueOf(millis);
            if (r.contains(".")) r = r.substring(0, r.indexOf(".") + 2);
            return r;
        }
        if (s.equals("bot_api")) return "JavaCord";
        if (s.equals("bot_prefix")) return DiscordModule.getConfig().getBotLayout().getPrefix();
        if (s.equals("bot_name")) return DiscordHandler.getBotUser().getName();
        if (s.equals("bot_author_name")) return DiscordHandler.getUser(138397636955865089L).getName();
        if (s.equals("bot_author_name_tagged")) return DiscordHandler.getUser(138397636955865089L).getName() + "#" + DiscordHandler.getUser(138397636955865089L).getDiscriminator();
        if (s.equals("bot_name_tagged")) return DiscordHandler.getBotUser().getName() + "#" + DiscordHandler.getBotUser().getDiscriminator();
        if (s.equals("bot_avatar_url")) return DiscordModule.getConfig().getBotLayout().getAvatarUrl();
        if (s.equals("bot_joined_guilds")) return String.valueOf(DiscordHandler.getJoinedServers().size());
        if (s.equals("bot_messages_out"))
            return String.valueOf(DiscordModule.getBotStats().getMessagesSentStat().getOrGet());
        if (s.equals("bot_messages_in_humans"))
            return String.valueOf(DiscordModule.getBotStats().getMessagesRecievedStat().getOrGet());
        if (s.equals("bot_messages_in_bots"))
            return String.valueOf(DiscordModule.getBotStats().getBotMessagesRecievedStat().getOrGet());
        if (s.equals("route_normal_count")) {
            AtomicInteger integer = new AtomicInteger(0);
            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                integer.getAndAdd(folder.getLoadedRoutes().size());
            });
            return String.valueOf(integer.get());
        }
        if (s.equals("route_events_count")) {
            AtomicInteger integer = new AtomicInteger(0);
            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                integer.getAndAdd(folder.getLoadedEventRoutes().size());
            });
            return String.valueOf(integer.get());
        }
        if (s.equals("channel_folders_count")) {
            return String.valueOf(DiscordHandler.getLoadedChanneledFolders().size());
        }

        return null;
    }

    @Override
    public String onRequest(StreamlineUser streamlineUser, String s) {
        ConcurrentSkipListSet<Long> userIds = DiscordModule.getVerifiedUsers().getDiscordIdsOf(streamlineUser.getUuid());
        if (s.equals("user_avatar_url"))
            return SLAPI.getRatAPI().parseAllPlaceholders(streamlineUser, DiscordModule.getConfig().getAvatarUrl()).join();
        if (s.equals("user_verification_code")) return DiscordHandler.getOrGetVerification(streamlineUser);
        if (s.equals("user_name")) {
            if (userIds.isEmpty()) return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_NULL.get();
            return DiscordHandler.getUser(userIds.first()).getName();
        }
        if (s.equals("user_name_tagged")) {
            if (userIds.isEmpty()) return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_NULL.get();
            return DiscordHandler.getUser(userIds.first()).getName() + "#" + DiscordHandler.getUser(userIds.first()).getDiscriminator();
        }
        return null;
    }
}
