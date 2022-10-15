package tv.quaint.discordmodule.server.events.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ChanneledFolder;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPoint;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ServerEventRoute;

public class SpigotEventManager {

    public static void loadAllSpigot() {
        if (! DiscordHandler.isBackEnd()) return;

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

    public static Plugin getStreamlineCorePlugin() {
        if (! DiscordHandler.isBackEnd()) return null;
        return Bukkit.getPluginManager().getPlugin("StreamlineAPI");
    }
}
