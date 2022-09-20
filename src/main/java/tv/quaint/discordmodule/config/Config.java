package tv.quaint.discordmodule.config;

import net.streamline.api.SLAPI;
import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.hooks.DiscordHook;
import tv.quaint.discordmodule.hooks.HookHandler;
import tv.quaint.discordmodule.hooks.depends.GroupsDependency;
import tv.quaint.discordmodule.hooks.depends.MessagingDependency;

public class Config extends ModularizedConfig {
    public Config() {
        super(DiscordModule.getInstance(), "config.yml", false);
    }

    public BotLayout getBotLayout() {
        String token = getOrSetDefault("bot.token", "<put token here -- DO NOT GIVE THIS TO ANYONE>");
        String prefix = getOrSetDefault("bot.prefix", ">>");

        return new BotLayout(token, prefix);
    }

    public void saveBotLayout(BotLayout layout) {
        write("bot.token", layout.getToken());
        write("bot.prefix", layout.getPrefix());
    }

    public void getHooks() {
        getHookFor(new DiscordHook<>(SLAPI::getInstance), true);
        if (DiscordModule.getGroupsDependency().isPresent())
            getHookFor(new DiscordHook<>(DiscordModule.getGroupsDependency()::getApi), DiscordModule.getGroupsDependency().getApi().isEnabled());
        if (DiscordModule.getMessagingDependency().isPresent())
            getHookFor(new DiscordHook<>(DiscordModule.getMessagingDependency()::getApi), DiscordModule.getMessagingDependency().getApi().isEnabled());
    }

    public void getHookFor(DiscordHook<?> hook, boolean def) {
        HookHandler.hookInto(hook, getHookBoolValue(hook.getHookName(), def));
    }
    
    public boolean getHookBoolValue(String name, boolean def) {
        return getOrSetDefault("hooks." + name + ".enabled", def);
    }

    public String getAvatarUrl() {
        reloadResource();

        return getOrSetDefault("messaging.avatar-url", "https://minotar.net/helm/%streamline_user_uuid%/1024.png");
    }

    public boolean allowStreamlineChannelsToDiscord() {
        reloadResource();

        return getOrSetDefault("messaging.to-discord.streamline-channels", true) && HookHandler.isMessagingHooked();
    }

    public boolean allowDiscordToStreamlineChannels() {
        reloadResource();

        return getOrSetDefault("messaging.to-minecraft.streamline-channels", true) && HookHandler.isMessagingHooked();
    }

    public boolean allowStreamlineGuildsToDiscord() {
        reloadResource();

        return getOrSetDefault("messaging.to-discord.streamline-guilds", true) && HookHandler.isGroupsHooked();
    }

    public boolean allowDiscordToStreamlineGuilds() {
        reloadResource();

        return getOrSetDefault("messaging.to-minecraft.streamline-guilds", true) && HookHandler.isGroupsHooked();
    }

    public boolean allowStreamlinePartiesToDiscord() {
        reloadResource();

        return getOrSetDefault("messaging.to-discord.streamline-parties", true) && HookHandler.isGroupsHooked();
    }

    public boolean allowDiscordToStreamlineParties() {
        reloadResource();

        return getOrSetDefault("messaging.to-minecraft.streamline-parties", true) && HookHandler.isGroupsHooked();
    }
}
