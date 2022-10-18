package tv.quaint.discordmodule.server;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.StorageUtils;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.MessageUtils;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.messaging.DiscordProxiedMessage;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ServerEvent<T> {
    @Getter
    private final String identifier;
    @Getter @Setter
    private ConcurrentSkipListMap<Integer, SingleSet<Supplier<String>, Function<String, Boolean>>> registeredEvents = new ConcurrentSkipListMap<>();
    @Getter @Setter
    private boolean enabled = false;

    public ServerEvent(String identifier) {
        this.identifier = identifier;
    }

    public int subscribe(Supplier<String> supplier, Function<String, Boolean> function) {
        getRegisteredEvents().put(getRegisteredEvents().size() + 1, new SingleSet<>(supplier, function));
        return getRegisteredEvents().size();
    }

    abstract public void onEvent(T event);

    abstract public String pass(String format, T event);

    public void unsubscribe(int i) {
        getRegisteredEvents().remove(i);
    }

    public void pushEvents(T event) {
        DiscordModule.getInstance().logDebug("Pushing...");
        getRegisteredEvents().forEach((integer, set) -> {
            DiscordModule.getInstance().logDebug("Found an event...");
            set.getValue().apply(pass(set.getKey().get(), event));
        });
        DiscordModule.getInstance().logDebug("Done...");
    }

    public String getDefaultMessageFormat(String prefix) {
        if (defaultMessageFormat() == null) return "--file:" + prefix + "-" + defaultJsonFile();
        if (defaultMessageFormat().equals("")) return "--file:" + prefix + "-" + defaultJsonFile();
        return defaultMessageFormat();
    }

    abstract public String defaultMessageFormat();

    abstract public String defaultJsonFile();

    public String getForwardMessage(String of, boolean load) {
        if (isJsonFile(of)) {
            String file = getJsonFile(of);
            if (load) loadFile(file);
            return getJsonFromFile(file);
        } else {
            return of;
        }
    }

    public String getForwardMessage(String of) {
        return getForwardMessage(of, true);
    }

    public void forwardMessage(String toForward, String type, String identifier) {
        if (UserUtils.getOnlinePlayers().size() == 0) return;
        StreamlinePlayer player = UserUtils.getOnlinePlayers().firstEntry().getValue();
        if (player == null) return;
        new DiscordProxiedMessage(player, toForward, toForward, identifier).send();
    }

    public void loadFile(String name) {
        StorageUtils.ensureFileFromSelfModule(
                DiscordModule.getInstance(),
                DiscordHandler.getForwardedJsonsFolder(),
                new File(DiscordHandler.getForwardedJsonsFolder(), name),
                name
        );
    }

    public String getJsonFromFile(String fileName) {
        File[] files = DiscordHandler.getForwardedJsonsFolder().listFiles((dir, currentFile) -> currentFile.equals(fileName));

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
}
