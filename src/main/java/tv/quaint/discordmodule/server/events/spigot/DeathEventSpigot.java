package tv.quaint.discordmodule.server.events.spigot;

import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.UserUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.events.serverevents.DeathEventedEvent;
import tv.quaint.discordmodule.server.SpigotServerEvent;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

public class DeathEventSpigot extends SpigotServerEvent<DeathEventedEvent> implements BaseEventListener {
    public DeathEventSpigot() {
        super("death");
        ModuleUtils.listen(this, DiscordModule.getInstance());
        if (DiscordModule.getConfig().moduleForwardsEventsToProxy() && DiscordHandler.isBackEnd()) {
            String forwarded = DiscordModule.getMessages().forwardedSpigotDeath();
            String toForward = getForwardMessage(forwarded);
            subscribe(
                    () -> toForward,
                    (s) -> {
                        DiscordModule.getInstance().logDebug("Trying to send...");
                        if (UserUtils.getOnlineUsers().size() == 0) return false;
                        StreamlinePlayer player = UserUtils.getOnlinePlayers().firstEntry().getValue();
                        if (player == null) return false;
                        forwardMessage(s, EndPointType.SPECIFIC_NATIVE.toString(), player.getLatestServer());
                        forwardMessage(s, EndPointType.SPECIFIC_HANDLED.toString(), player.getLatestServer());
                        return true;
                    }
            );
        }
    }

    @Override
    public String pass(String format, DeathEventedEvent ev) {
        PlayerDeathEvent event = ev.getEv();

        Player player = event.getEntity();
        if (player != null) {
            format = parseOnPlayer(player, format);
            StreamlinePlayer streamlinePlayer = ModuleUtils.getOrGetPlayer(player.getName());
            if (streamlinePlayer != null) format = ModuleUtils.replaceAllPlayerBungee(streamlinePlayer, format);

            Entity e = player.getLastDamageCause().getEntity();

            if (e != null) {
                if (e instanceof Player p) {
                    format = parseOnPlayer(p, format, "killer");
                } else {
                    format = parseOnEntity(e, format, "killer");
                }
            }
        }

        String message = event.getDeathMessage();
        String keepExp = event.getKeepLevel() ?
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
        String keepInv = event.getKeepInventory() ?
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();


        format = format
                .replace("%this_death_message%", ModuleUtils.stripColor(message))
                .replace("%this_keep_experience%", ModuleUtils.stripColor(keepExp))
                .replace("%this_keep_inventory%", ModuleUtils.stripColor(keepInv))
                ;

        return format
                ;
    }

    @Override
    public String defaultMessageFormat() {
        return null;
    }

    @Override
    public String defaultJsonFile() {
        return "on-death.json";
    }

    @BaseProcessor
    @Override
    public void onEvent(DeathEventedEvent event) {
        pushEvents(event);
    }
}
