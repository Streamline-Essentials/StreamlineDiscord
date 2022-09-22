package tv.quaint.discordmodule;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.StorageUtils;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import tv.quaint.discordmodule.command.VerifyCommandMC;
import tv.quaint.discordmodule.config.Config;
import tv.quaint.discordmodule.config.Messages;
import tv.quaint.discordmodule.config.VerifiedUsers;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.events.MainListener;
import tv.quaint.discordmodule.discord.saves.BotStats;
import tv.quaint.discordmodule.hooks.depends.GroupsDependency;
import tv.quaint.discordmodule.hooks.depends.MessagingDependency;
import tv.quaint.discordmodule.placeholders.DiscordExpansion;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;

public class DiscordModule extends SimpleModule {
    @Getter @Setter
    private static DiscordModule instance;

    @Getter @Setter
    private static Config config;
    @Getter @Setter
    private static Messages messages;
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
    public void registerCommands() {
        setCommands(List.of(
                new VerifyCommandMC()
        ));
    }

    @Override
    public void onLoad() {
        setInstance(this);
        setDiscordExpansion(new DiscordExpansion());
    }

    @Override
    public void onEnable() {
        setConfig(new Config());
        setMessages(new Messages());
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

    public static void loadFile(String name) {
        StorageUtils.ensureFileFromSelfModule(
                getInstance(),
                getInstance().getDataFolder(),
                new File(getInstance().getDataFolder(), name),
                name
        );
    }

    public static String getJsonFromFile(String fileName) {
        File[] files = getInstance().getDataFolder().listFiles((dir, currentFile) -> currentFile.equals(fileName));

        if (files == null) return null;

        try {
            return JsonParser.parseReader(new JsonReader(new FileReader(files[0]))).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isJsonFile(String wholeInput) {
        if (! wholeInput.startsWith("--file:")) return false;
        wholeInput = wholeInput.substring("--file:".length());
        return wholeInput.endsWith(".json");
    }

    public static String getJsonFile(String wholeInput) {
        if (wholeInput.startsWith("--file:")) {
            wholeInput = wholeInput.substring("--file:".length());
        }
        if (! wholeInput.endsWith(".json")) {
            wholeInput = wholeInput + ".json";
        }
        return wholeInput;
    }
}
