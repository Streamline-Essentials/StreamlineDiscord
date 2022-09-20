package tv.quaint.discordmodule.discord;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.events.DiscordMessageEvent;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordHandler {
    @Getter @Setter
    private static DiscordApi discordAPI;

    @Getter @Setter
    private static ConcurrentSkipListMap<String, DiscordCommand> registeredCommands = new ConcurrentSkipListMap<>();

    public static long getBotId() {
        return getDiscordAPI().getClientId();
    }

    public static User getBotUser() {
        return getDiscordAPI().getYourself();
    }

    public static ConcurrentSkipListMap<Long, Server> getJoinedServers() {
        ConcurrentSkipListMap<Long, Server> r = new ConcurrentSkipListMap<>();

        for (Server server : DiscordHandler.getDiscordAPI().getServers()) {
            r.put(server.getId(), server);
        }

        return r;
    }

    public static Server getServerById(long id) {
        return getJoinedServers().get(id);
    }

    public static Optional<Channel> getChannelById(long id) {
        return getDiscordAPI().getChannelById(id);
    }

    public static Optional<ServerChannel> getServerChannelById(long id) {
        return getDiscordAPI().getServerChannelById(id);
    }

    public static void init() {
        kill();

        BotLayout layout = DiscordModule.getConfig().getBotLayout();
        setDiscordAPI(new DiscordApiBuilder().setToken(layout.getToken()).login().join());

        getDiscordAPI().addMessageCreateListener(e -> {
            Optional<User> optionalUser = e.getMessageAuthor().asUser();
            if (optionalUser.isEmpty()) return;
            ModuleUtils.fireEvent(new DiscordMessageEvent(new MessagedString(optionalUser.get(), e.getMessageContent())));
        });
//        getDiscordAPI().addJoin
    }

    public static void kill() {

    }

    public static void registerCommand(DiscordCommand command) {
        getRegisteredCommands().put(command.getCommandIdentifier(), command);
    }

    public static void unregisterCommand(String identifier) {
        getRegisteredCommands().remove(identifier);
    }

    public static boolean isRegistered(String identifier) {
        return getRegisteredCommands().containsKey(identifier);
    }

    public static boolean isRegisteredByAlias(String alias) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getRegisteredCommands().forEach((s, command) -> {
            if (command.getAliases().contains(alias)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public static DiscordCommand getCommandByAlias(String alias) {
        AtomicReference<DiscordCommand> atomicCommand = new AtomicReference<>(null);

        getRegisteredCommands().forEach((s, command) -> {
            if (command.getAliases().contains(alias)) atomicCommand.set(command);
        });

        return atomicCommand.get();
    }
}
