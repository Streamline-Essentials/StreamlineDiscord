package tv.quaint.discordmodule.placeholders;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

public class DiscordExpansion extends RATExpansion {
    public DiscordExpansion() {
        super("discord", "Quaint", "0.0.1");
    }

    @Override
    public String onLogic(String s) {
        if (s.equals("bot_ping")) return String.valueOf(DiscordHandler.safeDiscordAPI().getLatestGatewayLatency());
        if (s.equals("bot_api")) return "JavaCord";
        if (s.equals("bot_joined_guilds")) return String.valueOf(DiscordHandler.getJoinedServers().size());
        if (s.equals("bot_messages_out")) return String.valueOf(DiscordModule.getBotStats().getMessagesSentStat().getOrGet());
        if (s.equals("bot_messages_in_humans")) return String.valueOf(DiscordModule.getBotStats().getMessagesRecievedStat().getOrGet());
        if (s.equals("bot_messages_in_bots")) return String.valueOf(DiscordModule.getBotStats().getBotMessagesRecievedStat().getOrGet());

        return null;
    }

    @Override
    public String onRequest(StreamlineUser streamlineUser, String s) {
        if (s.equals("user_avatar_url")) ModuleUtils.replaceAllPlayerBungee(streamlineUser, DiscordModule.getConfig().getAvatarUrl());
        return null;
    }
}
