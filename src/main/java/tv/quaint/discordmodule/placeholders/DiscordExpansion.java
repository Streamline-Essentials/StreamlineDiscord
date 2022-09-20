package tv.quaint.discordmodule.placeholders;

import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.discord.DiscordHandler;

public class DiscordExpansion extends RATExpansion {
    public DiscordExpansion() {
        super("discord", "Quaint", "0.0.1");
    }

    @Override
    public String onLogic(String s) {
        if (s.equals("bot_ping")) return String.valueOf(DiscordHandler.getDiscordAPI().getLatestGatewayLatency());
        if (s.equals("bot_api")) return "JavaCord";
        if (s.equals("bot_joined_guilds")) return String.valueOf(DiscordHandler.getJoinedServers().size());

        return null;
    }

    @Override
    public String onRequest(StreamlineUser streamlineUser, String s) {
        return null;
    }
}
