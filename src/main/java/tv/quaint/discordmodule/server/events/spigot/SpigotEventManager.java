package tv.quaint.discordmodule.server.events.spigot;

import net.streamline.api.scheduler.ModuleRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ChanneledFolder;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPoint;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ServerEventRoute;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SpigotEventManager implements Listener {
    public static class ServerEventInitializer extends ModuleRunnable {
        public ServerEventInitializer() {
            super(DiscordModule.getInstance(), 20, 0);
        }

        @Override
        public void run() {
            if (getStreamlinePluginAsFuture().join() == null) {
                DiscordModule.getInstance().logInfo("StreamlineCore not found! Delaying...");
                new ServerEventInitializer();
                cancel();
            }

            Bukkit.getPluginManager().registerEvents(new SpigotEventListener(), getStreamlinePluginAsFuture().join());
            cancel();
        }
    }

    public static void loadAllSpigot() {
        if (! DiscordHandler.isBackEnd()) return;

        demandLoadAllSpigot();

        if (getStreamlinePluginAsFuture().join() == null) {
            new ServerEventInitializer();
        } else {
            Bukkit.getPluginManager().registerEvents(new SpigotEventListener(), getStreamlinePluginAsFuture().join());
        }
    }

    public static void demandLoadAllSpigot() {
//        DiscordModule.getInstance().logInfo("Demanding the load of all Spigot Events...");
        if (DiscordModule.getConfig().serverEventSpigotAdvancement()) DiscordHandler.registerServerEvent(new AdvancementEventSpigot());
        if (DiscordModule.getConfig().serverEventSpigotDeath()) DiscordHandler.registerServerEvent(new DeathEventSpigot());
    }

    public static void unloadAllSpigot() {
        if (! DiscordHandler.isBackEnd()) return;

        if (DiscordModule.getConfig().serverEventSpigotAdvancement()) DiscordHandler.unregisterServerEvent("advancement");
        if (DiscordModule.getConfig().serverEventSpigotDeath()) DiscordHandler.unregisterServerEvent("death");
    }

    public static void addAdvancementEvent(EndPoint other, ChanneledFolder folder) {
        if (! DiscordHandler.isBackEnd()) return;
        if (DiscordHandler.containsServerEvent("advancement")) {
            ServerEventRoute<AdvancementEventSpigot> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(AdvancementEventSpigot.class));
            folder.loadEventRoute(r);
        }
    }

    public static void addDeathEvent(EndPoint other, ChanneledFolder folder) {
        if (! DiscordHandler.isBackEnd()) return;
        if (DiscordHandler.containsServerEvent("death")) {
            ServerEventRoute<DeathEventSpigot> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(DeathEventSpigot.class));
            folder.loadEventRoute(r);
        }
    }

    public static Plugin getStreamlinePlugin() {
        if (! DiscordHandler.isBackEnd()) return null;
        return Bukkit.getPluginManager().getPlugin("StreamlineCore");
    }

    public static CompletableFuture<Plugin> getStreamlinePluginAsFuture() {
        return CompletableFuture.supplyAsync(SpigotEventManager::getStreamlinePlugin).completeOnTimeout(null, 500, TimeUnit.MILLISECONDS);
    }
}
