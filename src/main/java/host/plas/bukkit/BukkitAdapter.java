package host.plas.bukkit;

import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import host.plas.DiscordModule;
import host.plas.bukkit.events.BukkitListener;

public class BukkitAdapter {
    public static void init() {
        DiscordModule.getInstance().logInfo("Initializing BukkitAdapter...");

        Plugin plugin = getPlugin();

        Bukkit.getPluginManager().registerEvents(new BukkitListener(), plugin);

        DiscordModule.getInstance().logInfo("Initialized BukkitAdapter.");
    }

    public static SLAPIB getAPI() {
        return SLAPIB.getInstance();
    }

    public static Plugin getPlugin() {
        return SLAPIB.getPlugin();
    }
}
