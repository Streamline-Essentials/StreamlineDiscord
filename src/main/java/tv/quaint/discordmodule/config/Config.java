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
        long commandCentral = getOrSetDefault("bot.channels.central", 0L);

        return new BotLayout(token, prefix, commandCentral);
    }

    public void saveBotLayout(BotLayout layout) {
        write("bot.token", layout.getToken());
        write("bot.prefix", layout.getPrefix());
        write("bot.channels.central", layout.getControlCenter());
    }

    public void getHooks() {
        GroupsDependency gdep = new GroupsDependency();
        MessagingDependency mdep = new MessagingDependency();

        getHookFor(new DiscordHook<>(SLAPI::getInstance), true);
        if (gdep.isPresent()) getHookFor(new DiscordHook<>(gdep::getApi), gdep.getApi().isEnabled());
        if (mdep.isPresent()) getHookFor(new DiscordHook<>(mdep::getApi), mdep.getApi().isEnabled());
    }

    public void getHookFor(DiscordHook<?> hook, boolean def) {
        HookHandler.hookInto(hook, getHookBoolValue(hook.getHookName(), def));
    }
    
    public boolean getHookBoolValue(String name, boolean def) {
        return getOrSetDefault("hooks." + name + ".enabled", def);
    }
}
