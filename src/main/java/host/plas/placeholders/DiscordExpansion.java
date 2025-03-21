package host.plas.placeholders;

import host.plas.DiscordModule;
import host.plas.discord.DiscordHandler;
import host.plas.discord.saves.obj.channeling.RouteManager;
import singularity.configs.given.MainMessagesHandler;
import singularity.modules.ModuleUtils;
import singularity.placeholders.expansions.RATExpansion;
import singularity.placeholders.replaceables.IdentifiedReplaceable;
import singularity.placeholders.replaceables.IdentifiedUserReplaceable;

import java.util.concurrent.ConcurrentSkipListSet;

public class DiscordExpansion extends RATExpansion {
    public DiscordExpansion() {
        super(new RATExpansionBuilder("discord"));
    }

    @Override
    public void init() {
        new IdentifiedReplaceable(this, "bot_ping", (s) -> {
            double millis = DiscordHandler.safeDiscordAPI().getGatewayPing();
            String r = String.valueOf(millis);
            if (r.contains(".")) r = r.substring(0, r.indexOf(".") + 2);
            return r;
        }).register();
        new IdentifiedReplaceable(this, "bot_api", (s) -> "JDA (Java Discord API)").register();
        new IdentifiedReplaceable(this, "bot_prefix", (s) -> DiscordModule.getConfig().getBotLayout().getPrefix()).register();
        new IdentifiedReplaceable(this, "bot_name", (s) -> DiscordHandler.getBotUser().getName()).register();
        new IdentifiedReplaceable(this, "bot_name_tagged", (s) -> DiscordHandler.getBotUser().getName()
                + "#" + DiscordHandler.getBotUser().getDiscriminator()).register();
        new IdentifiedReplaceable(this, "bot_author_name", (s) -> DiscordHandler.getUser(138397636955865089L).getName()).register();
        new IdentifiedReplaceable(this, "bot_author_name_tagged", (s) -> DiscordHandler.getUser(138397636955865089L).getName()
                + "#" + DiscordHandler.getUser(138397636955865089L).getDiscriminator()).register();
        new IdentifiedReplaceable(this, "bot_avatar_url", (s) -> DiscordModule.getConfig().getBotLayout().getAvatarUrl()).register();
        new IdentifiedReplaceable(this, "bot_joined_guilds", (s) -> String.valueOf(DiscordHandler.getJoinedServers().size())).register();
        new IdentifiedReplaceable(this, "bot_author_avatar_url", (s) -> DiscordHandler.getUser(138397636955865089L).getAvatarUrl()).register();

        new IdentifiedReplaceable(this, "bot_messages_out", (s) -> String.valueOf(DiscordModule.getBotStats().getMessagesSentStat().getOrGet())).register();
        new IdentifiedReplaceable(this, "bot_messages_in_humans", (s) -> String.valueOf(DiscordModule.getBotStats().getMessagesRecievedStat().getOrGet())).register();
        new IdentifiedReplaceable(this, "bot_messages_in_bots", (s) -> String.valueOf(DiscordModule.getBotStats().getBotMessagesRecievedStat().getOrGet())).register();

        new IdentifiedReplaceable(this, "route_normal_count", (s) -> {
            return String.valueOf(RouteManager.getLoadedRoutes().size());
        }).register();

        new IdentifiedUserReplaceable(this, "user_avatar_url", (s, u) -> ModuleUtils.replacePlaceholders(u, DiscordModule.getConfig().getAvatarUrl())).register();
        new IdentifiedUserReplaceable(this, "user_verification_code", (s, u) -> DiscordHandler.getOrGetVerification(u)).register();

        new IdentifiedUserReplaceable(this, "user_name", (s, u) -> {
            ConcurrentSkipListSet<Long> userIds = DiscordModule.getVerifiedUsers().getDiscordIdsOf(u.getUuid());

            if (userIds.isEmpty()) return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_NULL.get();
            return DiscordHandler.getUser(userIds.first()).getName();
        }).register();
        new IdentifiedUserReplaceable(this, "user_name_tagged", (s, u) -> {
            ConcurrentSkipListSet<Long> userIds = DiscordModule.getVerifiedUsers().getDiscordIdsOf(u.getUuid());

            if (userIds.isEmpty()) return MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_NULL.get();
            return DiscordHandler.getUser(userIds.first()).getName() + "#" + DiscordHandler.getUser(userIds.first()).getDiscriminator();
        }).register();
    }
}
