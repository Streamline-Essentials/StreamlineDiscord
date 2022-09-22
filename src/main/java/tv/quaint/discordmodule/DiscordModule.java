package tv.quaint.discordmodule;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import tv.quaint.discordmodule.config.Config;
import tv.quaint.discordmodule.config.VerifiedUsers;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MainListener;
import tv.quaint.discordmodule.discord.saves.BotStats;
import tv.quaint.discordmodule.hooks.depends.GroupsDependency;
import tv.quaint.discordmodule.hooks.depends.MessagingDependency;
import tv.quaint.discordmodule.placeholders.DiscordExpansion;

import java.util.Collections;
import java.util.List;

public class DiscordModule extends SimpleModule {
    @Getter @Setter
    private static DiscordModule instance;

    @Getter @Setter
    private static Config config;
    @Getter @Setter
    private static BotStats botStats;
    @Getter @Setter
    private static VerifiedUsers verifiedUsers;

    @Getter @Setter
    private static GroupsDependency groupsDependency;
    @Getter @Setter
    private static MessagingDependency messagingDependency;

    @Getter @Setter
    private static DiscordExpansion discordExpansion;
    @Getter @Setter
    private static MainListener mainListener;

    @Override
    public String identifier() {
        return "streamline-discord";
    }

    @Override
    public List<String> authors() {
        return List.of("Quaint");
    }

    @Override
    public List<Dependency> dependencies() {
        return Collections.emptyList();
    }

    @Override
    public void onLoad() {
        setInstance(this);
        setDiscordExpansion(new DiscordExpansion());
    }

    @Override
    public void onEnable() {
        setConfig(new Config());
        setBotStats(new BotStats());
        setVerifiedUsers(new VerifiedUsers());
        setGroupsDependency(new GroupsDependency());
        setMessagingDependency(new MessagingDependency());

        getDiscordExpansion().register();

        DiscordHandler.init();

        setMainListener(new MainListener());
        ModuleUtils.listen(getMainListener(), this);
    }

    @Override
    public void onDisable() {
        DiscordHandler.getRegisteredCommands().forEach((s, command) -> {
            command.unregister();
        });

        DiscordHandler.kill();
        getDiscordExpansion().unregister();
    }
}
