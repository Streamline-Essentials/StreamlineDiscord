package tv.quaint.discordmodule.config;

import net.streamline.api.SLAPI;
import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.hooks.DiscordHook;
import tv.quaint.discordmodule.hooks.HookHandler;
import tv.quaint.discordmodule.hooks.depends.GroupsDependency;
import tv.quaint.discordmodule.hooks.depends.MessagingDependency;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(DiscordModule.getInstance(), "messages.yml", true);
    }
}
