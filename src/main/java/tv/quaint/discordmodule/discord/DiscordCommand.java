package tv.quaint.discordmodule.discord;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.streamline.api.SLAPI;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.configs.StreamlineStorageUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.storage.StorageUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class DiscordCommand extends ModularizedConfig {

    @Getter @Setter
    private boolean enabled;
    @Getter @Setter
    private String commandIdentifier;
    @Getter @Setter
    private ConcurrentSkipListSet<String> aliases;
    @Getter @Setter
    private long role;

    public DiscordCommand(String commandIdentifier, String... aliases) {
        this(commandIdentifier, 0L, aliases);
    }

    public DiscordCommand(String commandIdentifier, long role, String... aliases) {
        this(commandIdentifier, new ConcurrentSkipListSet<>(Arrays.stream(aliases).toList()), role);
    }

    public DiscordCommand(String commandIdentifier, ConcurrentSkipListSet<String> aliases, long role) {
        super(DiscordModule.getInstance(), commandIdentifier + ".yml",
                DiscordHandler.getDiscordCommandFolder(commandIdentifier), false);
        setCommandIdentifier(commandIdentifier);
        setAliases(aliases);
        setRole(role);

        loadCommand(true);

        register();
    }

    public void loadCommand() {
        loadCommand(false);
    }

    public void loadCommand(boolean force) {
        reloadResource(force);

        setEnabled(getResource().getOrSetDefault("enabled", true));
        setRole(getResource().getOrSetDefault("permissions.default", getRole()));
        setAliases(new ConcurrentSkipListSet<>(getResource().getOrSetDefault("aliases", getAliases().stream().toList())));
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

    public boolean hasDefaultPermissions() {
        return getRole() == 0L;
    }

    public boolean defaultPermissionIsServerOwner() {
        return getRole() == -1L;
    }

    public void execute(MessagedString messagedString) {
        if (hasDefaultPermissions()) {
            executeMore(messagedString);
            return;
        }
        if (defaultPermissionIsServerOwner()) {
            if (messagedString.getChannel() instanceof TextChannel serverTextChannel) {
                if (serverTextChannel.getGuild().getOwnerId().equals(messagedString.getAuthor().getId()))
                    executeMore(messagedString);
            }
            return;
        }
        if (messagedString.getChannel() instanceof TextChannel serverTextChannel) {
            Role role = serverTextChannel.getGuild().getRoleById(getRole());
            if (role == null) return;
            if (! serverTextChannel.getGuild().isMember(messagedString.getAuthor())) return;
            Member member = serverTextChannel.getGuild().getMember(messagedString.getAuthor());
            if (member == null) return;
            if (member.getRoles().contains(role)) executeMore(messagedString);
        }
    }

    public abstract void executeMore(MessagedString messagedString);

    public void ensureJsonFile(String fileName) {
        try {
            SLAPI.getInstance().getResourceAsStream(fileName).close();
        } catch (Exception e) {
            return;
        }

        StorageUtils.ensureFileFromSelf(DiscordModule.getInstance().getWrapper().getPluginClassLoader(), DiscordHandler.getDiscordCommandFolder(getCommandIdentifier()),
                new File(DiscordHandler.getDiscordCommandFolder(getCommandIdentifier()), fileName), fileName);
    }

    public File getFolder() {
        return DiscordHandler.getDiscordCommandFolder(getCommandIdentifier());
    }

    public String getJsonFromFile(String fileName) {
        File[] files = DiscordHandler.getDiscordCommandFolder(getCommandIdentifier()).listFiles((dir, currentFile) -> currentFile.equals(fileName));

        if (files == null) return null;

        try {
            return JsonParser.parseReader(new JsonReader(new FileReader(files[0]))).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isJsonFile(String wholeInput) {
        if (! wholeInput.startsWith("--file:")) return false;
        wholeInput = wholeInput.substring("--file:".length());
        return wholeInput.endsWith(".json");
    }

    public String getJsonFile(String wholeInput) {
        if (wholeInput.startsWith("--file:")) {
            wholeInput = wholeInput.substring("--file:".length());
        }
        if (! wholeInput.endsWith(".json")) {
            wholeInput = wholeInput + ".json";
        }
        return wholeInput;
    }

    public void loadFile(String name) {
        StreamlineStorageUtils.ensureFileFromSelfModule(
                DiscordModule.getInstance(),
                getFolder(),
                new File(getFolder(), name),
                name
        );
    }
}
