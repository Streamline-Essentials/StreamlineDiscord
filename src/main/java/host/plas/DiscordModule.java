package host.plas;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import org.pf4j.PluginWrapper;
import host.plas.bukkit.BukkitAdapter;
import host.plas.command.CreateChannelCommandMC;
import host.plas.command.UnVerifyCommandMC;
import host.plas.command.VerifyCommandMC;
import host.plas.config.Config;
import host.plas.config.Messages;
import host.plas.config.VerifiedUsers;
import host.plas.database.EndPointKeeper;
import host.plas.database.RouteKeeper;
import host.plas.depends.MessagingDependency;
import host.plas.discord.DiscordHandler;
import host.plas.discord.saves.BotStats;
import host.plas.discord.saves.obj.channeling.RouteManager;
import host.plas.events.MainListener;
import host.plas.placeholders.DiscordExpansion;
import singularity.modules.SimpleModule;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Scanner;
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

//    @Getter @Setter
//    private static GroupsDependency groupsDependency;
    @Getter @Setter
    private static MessagingDependency messagingDependency;

    @Getter @Setter
    private static DiscordExpansion discordExpansion;
    @Getter @Setter
    private static MainListener mainListener;

    @Getter @Setter
    private static RouteKeeper routeKeeper;
    @Getter @Setter
    private static EndPointKeeper endPointKeeper;

    public DiscordModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
//        setGroupsDependency(new GroupsDependency());
        setMessagingDependency(new MessagingDependency());

        setConfig(new Config());
        setMessages(new Messages());
        setBotStats(new BotStats());
        setVerifiedUsers(new VerifiedUsers());

        setRouteKeeper(new RouteKeeper());
        setEndPointKeeper(new EndPointKeeper());

        if (getConfig().getBotLayout().getMainGuildId() == 0L) {
            logWarning("&bWARNING!&r &dYou need to set the main guild ID in the config.yml file for the Discord Streamline module!&r%newline%&cDisabling the Discord Streamline module...&r");
            super.stop();
            return;
        }

        RouteManager.loadAllRoutes();

        setDiscordExpansion(new DiscordExpansion());
        getDiscordExpansion().init();

        DiscordHandler.init().completeOnTimeout(false, 15, TimeUnit.SECONDS).join(); // no need to announce if it fails. works anyway?

        setMainListener(new MainListener());

        new VerifyCommandMC().register();
        new CreateChannelCommandMC().register();
        new UnVerifyCommandMC().register();

        if (! SLAPI.isProxy()) {
            BukkitAdapter.init();
        }
    }

    @Override
    public void onDisable() {
        DiscordHandler.kill().completeOnTimeout(false, 7, TimeUnit.SECONDS).join();
        if (getDiscordExpansion() != null) getDiscordExpansion().stop();
    }

    public static void loadFile(String name) {
        loadFile(getInstance().getDataFolder(), name, name);
    }

    public static void loadFile(String selfName, String newName) {
        loadFile(getInstance().getDataFolder(), selfName, newName);
    }

    public static void loadFile(File parentFolder, String selfName, String newName) {
        try {
            File file = new File(parentFolder, newName);

            InputStream stream = SLAPI.class.getClassLoader().getResourceAsStream(selfName);
            if (stream == null) return;

            try (FileWriter writer = new FileWriter(file)) {
                Scanner scanner = new Scanner(stream);
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
