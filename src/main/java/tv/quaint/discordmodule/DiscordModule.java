package tv.quaint.discordmodule;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import tv.quaint.discordmodule.config.Config;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.BotStats;

import java.util.Collections;
import java.util.List;

public class DiscordModule extends SimpleModule {
    @Getter @Setter
    private static DiscordModule instance;

    @Getter @Setter
    private static Config config;
    @Getter @Setter
    private static BotStats botStats;

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
    }

    @Override
    public void onEnable() {
        setConfig(new Config());
        setBotStats(new BotStats());
    }

    @Override
    public void onDisable() {
        DiscordHandler.getRegisteredCommands().forEach((s, command) -> {
            command.
        });
    }
}
