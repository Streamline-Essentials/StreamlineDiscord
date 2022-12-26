package tv.quaint.discordmodule.config;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.discordmodule.DiscordModule;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(DiscordModule.getInstance(), "messages.yml", true);

        init();
    }

    public void init() {
        successMinecraft();
        successDiscord();

        forwardedStreamlineLogin();
        forwardedStreamlineLogout();
        forwardedSpigotAdvancement();
        forwardedSpigotDeath();
    }

    public String successMinecraft() {
        reloadResource();

        return getOrSetDefault("verification.success.minecraft", "&a&lSuccess&8! &eYou verified &d%streamline_user_absolute% &eas &d%discord_user_name_tagged%");
    }

    public String successDiscord() {
        reloadResource();

        return getOrSetDefault("verification.success.discord", "--file:verify-success-response.json");
    }

    public String failureGenericDiscord() {
        reloadResource();

        return getOrSetDefault("verification.failure.generic", "--file:verify-failure-response.json");
    }

    public String failureAlreadyVerifiedDiscord() {
        reloadResource();

        return getOrSetDefault("verification.failure.already-verified", "--file:verify-already-response.json");
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
