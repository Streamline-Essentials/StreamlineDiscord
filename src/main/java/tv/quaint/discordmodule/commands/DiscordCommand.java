package tv.quaint.discordmodule.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class DiscordCommand extends ModularizedConfig {
    @Getter
    private static final File mainFolder = new File(DiscordModule.getInstance().getDataFolder(), "discord-commands" + File.separator);

    @Getter @Setter
    private boolean enabled;
    @Getter @Setter
    private String commandIdentifier;
    @Getter @Setter
    private ConcurrentSkipListSet<String> aliases;
    @Getter @Setter
    private long role;

    public DiscordCommand(String commandIdentifier) {
        super(DiscordModule.getInstance(), commandIdentifier + ".yml", getMainFolder(), false);
        setCommandIdentifier(commandIdentifier);

        loadCommand(true);

        register();
    }

    public void loadCommand() {
        loadCommand(false);
    }

    public void loadCommand(boolean force) {
        reloadResource(force);

        setEnabled(resource.getOrDefault("enabled", true));
        setRole(resource.getOrDefault("permissions.default", 0L));
        setAliases(new ConcurrentSkipListSet<>(resource.getOrDefault("aliases", List.of(getCommandIdentifier()))));
    }

    public void saveCommand() {
        write("enabled", isEnabled());
        write("permissions.default", getRole());
        write("aliases", getAliases().stream().toList());
    }

    public void register() {
        if (! isEnabled()) return;
        DiscordHandler.registerCommand(this);
    }

    public void unregister() {
        DiscordHandler.unregisterCommand(this.getCommandIdentifier());
    }

    public boolean isRegistered() {
        return DiscordHandler.isRegistered(this.getCommandIdentifier());
    }

    public abstract void execute(MessagedString messagedString);
}
