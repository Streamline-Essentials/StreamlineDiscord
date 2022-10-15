package tv.quaint.discordmodule.config;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.discordmodule.DiscordModule;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(DiscordModule.getInstance(), "messages.yml", true);

        init();
    }

    public void init() {
        completedMinecraft();
        completedDiscord();

        forwardedStreamlineLogin();
        forwardedStreamlineLogout();
        forwardedSpigotAdvancement();
        forwardedSpigotDeath();
    }

    public String completedMinecraft() {
        reloadResource();

        return getOrSetDefault("verification.completed.minecraft", "&a&lSuccess&8! &eYou verified &d%streamline_user_absolute% &eas &d%discord_user_name_tagged%");
    }

    public String completedDiscord() {
        reloadResource();

        return getOrSetDefault("verification.completed.discord", "--file:verified-response.json");
    }

    public String forwardedStreamlineLogin() {
        reloadResource();

        return getOrSetDefault("forwarded.streamline.login", "--file:on-login.json");
    }

    public String forwardedStreamlineLogout() {
        reloadResource();

        return getOrSetDefault("forwarded.streamline.logout", "--file:on-logout.json");
    }

    public String forwardedSpigotAdvancement() {
        reloadResource();

        return getOrSetDefault("forwarded.spigot.advancement", "--file:on-advancement.json");
    }

    public String forwardedSpigotDeath() {
        reloadResource();

        return getOrSetDefault("forwarded.spigot.death", "--file:on-death.json");
    }
}
