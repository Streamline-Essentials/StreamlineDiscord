package tv.quaint.discordmodule.server.events.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.events.serverevents.AdvancementEventedEvent;
import tv.quaint.discordmodule.events.serverevents.DeathEventedEvent;
import tv.quaint.discordmodule.events.serverevents.ServerEventedEvent;

public class SpigotEventListener implements Listener {
    public SpigotEventListener() {
        DiscordModule.getInstance().logInfo("Registered " + getClass().getSimpleName() + "!");
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        AdvancementEventedEvent e = new AdvancementEventedEvent("advancement", event);
        e = e.fire();
        if (e.isCancelled()) return;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DeathEventedEvent e = new DeathEventedEvent("death", event);
        e = e.fire();
        if (e.isCancelled()) return;
    }
}
