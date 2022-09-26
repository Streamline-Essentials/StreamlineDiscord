package tv.quaint.discordmodule.config;

import net.streamline.api.SLAPI;
import net.streamline.api.configs.ModularizedConfig;
import org.javacord.api.entity.activity.ActivityType;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.hooks.DiscordHook;
import tv.quaint.discordmodule.hooks.HookHandler;

public class Config extends ModularizedConfig {
    public Config() {
        super(DiscordModule.getInstance(), "config.yml", true);

        getHooks();
    }

    public BotLayout getBotLayout() {
        String token = getOrSetDefault("bot.token", "<put token here -- DO NOT GIVE THIS TO ANYONE>");
        String prefix = getOrSetDefault("bot.prefix", ">>");
        ActivityType activityType = ActivityType.valueOf(getOrSetDefault("bot.activity.type", ActivityType.CUSTOM.toString()));
        String activityValue = getOrSetDefault("bot.activity.value", "**" + prefix + "help** for help!");

        return new BotLayout(token, prefix, activityType, activityValue);
    }

    public void saveBotLayout(BotLayout layout) {
        write("bot.token", layout.getToken());
        write("bot.prefix", layout.getPrefix());
        write("bot.activity.type", layout.getActivityType().toString());
        write("bot.activity.value", layout.getActivityValue());
    }

    public void getHooks() {
        getHookFor(new DiscordHook<>(SLAPI::getInstance, "streamline-base"), true);
        if (DiscordModule.getGroupsDependency().isPresent())
            getHookFor(new DiscordHook<>(DiscordModule.getGroupsDependency()::getApi, "streamline-groups"), DiscordModule.getGroupsDependency().getApi().isEnabled());
        if (DiscordModule.getMessagingDependency().isPresent())
            getHookFor(new DiscordHook<>(DiscordModule.getMessagingDependency()::getApi, "streamline-messaging"), DiscordModule.getMessagingDependency().getApi().isEnabled());
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

    public String getDefaultFormatMinecraft() {
        reloadResource();

        return getOrSetDefault("messaging.default-format.from-minecraft", "%streamline_user_absolute%: %this_message%");
    }

    public String getDefaultFormatDiscord() {
        reloadResource();

        return getOrSetDefault("messaging.default-format.from-minecraft", "&8[&9&lDiscord&8] &d%streamline_user_absolute% &9>> &r%this_message%");
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
