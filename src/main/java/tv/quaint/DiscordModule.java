package tv.quaint;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.StreamlineStorageUtils;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import tv.quaint.command.CreateChannelCommandMC;
import tv.quaint.command.UnVerifyCommandMC;
import tv.quaint.command.VerifyCommandMC;
import tv.quaint.config.Config;
import tv.quaint.config.Messages;
import tv.quaint.config.VerifiedUsers;
import tv.quaint.depends.GroupsDependency;
import tv.quaint.depends.MessagingDependency;
import tv.quaint.discord.DiscordHandler;
import tv.quaint.discord.saves.BotStats;
import tv.quaint.events.MainListener;
import tv.quaint.placeholders.DiscordExpansion;
import net.streamline.thebase.lib.google.gson.JsonParser;
import net.streamline.thebase.lib.google.gson.stream.JsonReader;
import net.streamline.thebase.lib.pf4j.PluginWrapper;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public DiscordModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new VerifyCommandMC(),
                new CreateChannelCommandMC(),
                new UnVerifyCommandMC()
        ));
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        setGroupsDependency(new GroupsDependency());
        setMessagingDependency(new MessagingDependency());

        setConfig(new Config());
        setMessages(new Messages());
        setBotStats(new BotStats());
        setVerifiedUsers(new VerifiedUsers());

        if (getConfig().getBotLayout().getMainGuildId() == 0L) {
            logWarning("&bWARNING!&r &dYou need to set the main guild ID in the config.yml file for the Discord Streamline module!&r%newline%&cDisabling the Discord Streamline module...&r");
            super.stop();
            return;
        }

        setDiscordExpansion(new DiscordExpansion());
        getDiscordExpansion().init();

        DiscordHandler.init().completeOnTimeout(false, 15, TimeUnit.SECONDS).join(); // no need to announce if it fails. works anyway?

        setMainListener(new MainListener());
        ModuleUtils.listen(getMainListener(), this);
    }

    @Override
    public void onDisable() {
        DiscordHandler.kill().completeOnTimeout(false, 7, TimeUnit.SECONDS).join();
        getDiscordExpansion().stop();
    }

    public static void loadFile(String name) {
        loadFile(getInstance().getDataFolder(), name, name);
    }

    public static void loadFile(String selfName, String newName) {
        loadFile(getInstance().getDataFolder(), selfName, newName);
    }

    public static void loadFile(File parentFolder, String selfName, String newName) {
        StreamlineStorageUtils.ensureFileFromSelfModule(
                getInstance(),
                parentFolder,
                new File(getInstance().getDataFolder(), newName),
                selfName
        );
    }

    public static String getJsonFromFile(String fileName) {
        return getJsonFromFile(getInstance().getDataFolder(), fileName);
    }

    public static String getJsonFromFile(File parentFolder, String fileName) {
        File[] files = parentFolder.listFiles((dir, currentFile) -> currentFile.equals(fileName));

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
