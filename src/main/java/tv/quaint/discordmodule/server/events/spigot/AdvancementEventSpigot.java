package tv.quaint.discordmodule.server.events.spigot;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.UserUtils;
import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.server.SpigotServerEvent;

public class AdvancementEventSpigot extends SpigotServerEvent<PlayerAdvancementDoneEvent> implements Listener {
    public AdvancementEventSpigot() {
        super("advancement");
        Bukkit.getPluginManager().registerEvents(this, SpigotEventManager.getStreamlineCorePlugin());
        if (DiscordModule.getConfig().moduleForwardsEventsToProxy() && DiscordHandler.isBackEnd()) {
            String forwarded = DiscordModule.getMessages().forwardedSpigotAdvancement();
            String toForward = getForwardMessage(forwarded);
            subscribe(
                    () -> toForward,
                    (s) -> {
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
    public String pass(String format, PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            format = parseOnPlayer(player, format);
            StreamlinePlayer streamlinePlayer = ModuleUtils.getOrGetPlayer(player.getName());
            if (streamlinePlayer != null) format = ModuleUtils.replaceAllPlayerBungee(streamlinePlayer, format);
        }

        Advancement advancement = event.getAdvancement();
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null) format = format
                .replace("%this_advancement_title%", ModuleUtils.stripColor(advancement.getDisplay().getTitle()))
                .replace("%this_advancement_description%", ModuleUtils.stripColor(advancement.getDisplay().getDescription()))
                .replace("%this_advancement_criteria%", ModuleUtils.stripColor(ModuleUtils.getListAsFormattedString(advancement.getCriteria().stream().toList())))
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
        return "on-advancement.json";
    }

    @EventHandler
    @Override
    public void onEvent(PlayerAdvancementDoneEvent event) {
        pushEvents(event);
    }
}
