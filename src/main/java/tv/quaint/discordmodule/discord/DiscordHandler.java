package tv.quaint.discordmodule.discord;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.commands.ChannelCommand;
import tv.quaint.discordmodule.discord.commands.PingCommand;
import tv.quaint.discordmodule.discord.commands.ReloadCommand;
import tv.quaint.discordmodule.discord.commands.RestartCommand;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPoint;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;
import tv.quaint.discordmodule.events.DiscordMessageEvent;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordHandler {
    @Getter @Setter
    private static AtomicReference<DiscordApi> concurrentDiscordAPI;

    public static DiscordApi getDiscordAPI() {
        if (getConcurrentDiscordAPI() == null) return null;
        return getConcurrentDiscordAPI().get();
    }

    public static void setDiscordAPI(DiscordApi value) {
        if (getConcurrentDiscordAPI() == null) {
            setConcurrentDiscordAPI(new AtomicReference<>(value));
            return;
        }
        getConcurrentDiscordAPI().set(value);
    }

    @NonNull
    public static DiscordApi safeDiscordAPI() {
        return Objects.requireNonNull(getDiscordAPI(), "The Concurrent DiscordAPI is 'null'!");
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, DiscordCommand> registeredCommands = new ConcurrentSkipListMap<>();

    public static long getBotId() {
        return safeDiscordAPI().getClientId();
    }

    public static User getBotUser() {
        return safeDiscordAPI().getYourself();
    }

    public static long getUserId(User user) {
        return user.getId();
    }

    public static User getUser(long userId) {
        return safeDiscordAPI().getUserById(userId).join();
    }

    public static ConcurrentSkipListMap<Long, Server> getJoinedServers() {
        ConcurrentSkipListMap<Long, Server> r = new ConcurrentSkipListMap<>();

        for (Server server : DiscordHandler.safeDiscordAPI().getServers()) {
            r.put(server.getId(), server);
        }

        return r;
    }

    public static Server getServerById(long id) {
        return getJoinedServers().get(id);
    }

    public static Optional<Channel> getChannelById(long id) {
        return safeDiscordAPI().getChannelById(id);
    }

    public static Optional<ServerChannel> getServerChannelById(long id) {
        return safeDiscordAPI().getServerChannelById(id);
    }

    public static Optional<TextChannel> getTextChannelById(long id) {
        return safeDiscordAPI().getTextChannelById(id);
    }

    public static Optional<ServerTextChannel> getServerTextChannelById(long id) {
        return safeDiscordAPI().getServerTextChannelById(id);
    }

    public static void registerCommands() {
        new PingCommand();
        new ReloadCommand();
        new RestartCommand();
        new ChannelCommand();
    }

    public static CompletableFuture<Void> init() {
        return CompletableFuture.supplyAsync(() -> {
            kill().join();

            DiscordModule.getInstance().logInfo("Bot is initializing...!");

            BotLayout layout = DiscordModule.getConfig().getBotLayout();
            setDiscordAPI(new DiscordApiBuilder()
                    .setToken(layout.getToken())
                    .setAllIntents()
                    .login()
                    .join()
            );

            safeDiscordAPI().addMessageCreateListener(e -> {
                Optional<User> optionalUser = e.getMessageAuthor().asUser();
                if (optionalUser.isEmpty()) return;
                ModuleUtils.fireEvent(new DiscordMessageEvent(new MessagedString(optionalUser.get(), e.getMessageAuthor(), e.getChannel(), e.getMessageContent())));
            });
//        getDiscordAPI().addJoin

            safeDiscordAPI().updateActivity(layout.getActivityType(), layout.getActivityValue());

            registerCommands();

            loadAllRoutes();

            if (getDiscordAPI() != null) DiscordModule.getInstance().logInfo("Bot is initialized!");

            return null;
        });
    }

    public static CompletableFuture<Boolean> kill() {
        return CompletableFuture.supplyAsync(() -> {
            if (getDiscordAPI() == null) return false;

            getRegisteredCommands().forEach((s, command) -> {
                command.unregister();
            });

            killRoutes();

            safeDiscordAPI().disconnect().join();

            setConcurrentDiscordAPI(null);

            return true;
        });
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

    @Getter
    private static final File discordCommandMainFolder = new File(DiscordModule.getInstance().getDataFolder(), "discord-commands" + File.separator);

    public static File getDiscordCommandFolder(String commandIdentifier) {
        return new File(getDiscordCommandMainFolder(), commandIdentifier + File.separator);
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, String> pendingVerifications = new ConcurrentSkipListMap<>();

    public static String getOrGetVerification(StreamlineUser user) {
        return getOrGetVerification(user.getUuid());
    }

    public static String getOrGetVerification(String uuid) {
        String r = getPendingVerifications().get(uuid);
        if (r != null) return r;
        r = createVerification();
        getPendingVerifications().put(uuid, r);
        return r;
    }

    public static String createVerification() {
        String uuid = UUID.randomUUID().toString();
        String r = uuid.substring(uuid.lastIndexOf("-") + 8);
        if (hasVerification(r)) r = createVerification();
        return r;
    }

    public static boolean hasVerification(String verification) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getPendingVerifications().forEach((streamlineUser, s) -> {
            if (s.equals(verification)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public static StreamlineUser getPendingVerificationUser(String verification) {
        AtomicReference<StreamlineUser> atomicUser = new AtomicReference<>(null);

        getPendingVerifications().forEach((uuid, s) -> {
            if (s.equals(verification)) atomicUser.set(ModuleUtils.getOrGetUser(uuid));
        });

        return atomicUser.get();
    }

    public static CompletableFuture<Boolean> verifyUser(long discordId, String verification) {
        return CompletableFuture.supplyAsync(() -> {
            if (DiscordModule.getVerifiedUsers().isVerified(discordId)) return false;
            if (! hasVerification(verification)) return false;
            StreamlineUser user = getPendingVerificationUser(verification);
            DiscordModule.getVerifiedUsers().verifyUser(user.getUuid(), discordId).join();
            getPendingVerifications().remove(user);
            return true;
        });
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, Route> loadedRoutes = new ConcurrentSkipListMap<>();

    public static boolean loadRoute(Route route) {
        if (routeExists(route)) {
            DiscordModule.getInstance().logInfo("Not loading route '" + route.getUuid() + "' as it already is loaded.");
            return false;
        }
        getLoadedRoutes().put(route.getUuid(), route);
        return true;
    }

    public static void unloadRoute(String uuid) {
        getLoadedRoutes().remove(uuid);
    }

    public static ConcurrentSkipListSet<Route> getAssociatedRoutes(EndPointType type, String identifier) {
        ConcurrentSkipListSet<Route> r = new ConcurrentSkipListSet<>();

        getLoadedRoutes().forEach((s, route) -> {
            if (route.getInput().getType().equals(type) && route.getInput().getIdentifier().equals(identifier)) {
                r.add(route);
                return;
            }
            if (route.getOutput().getType().equals(type) && route.getOutput().getIdentifier().equals(identifier)) {
                r.add(route);
            }
        });

        return r;
    }

    public static ConcurrentSkipListSet<Route> getBackAndForthRoute(EndPointType type, String identifier, String channelId) {
        ConcurrentSkipListSet<Route> r = new ConcurrentSkipListSet<>();

        getAssociatedRoutes(type, identifier).forEach((route) -> {
            if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(channelId)) {
                r.add(route);
                return;
            }
            if (route.getOutput().getType().equals(EndPointType.DISCORD_TEXT) && route.getOutput().getIdentifier().equals(channelId)) {
                r.add(route);
            }
        });

        return r;
    }

    public static void killRoutes() {
        getLoadedRoutes().forEach((s, route) -> route.saveAll());
        setLoadedRoutes(new ConcurrentSkipListMap<>());
    }

    public static void loadAllRoutes() {
        killRoutes();

        File[] files = Route.getDataFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.isFile()) return;
            if (! file.getName().endsWith(".yml")) return;

            String uuid = file.getName().substring(0, file.getName().lastIndexOf("."));
            if (! loadRoute(new Route(uuid))) DiscordModule.getInstance().logWarning("Could not load a route with a UUID of '" + uuid + "'.");
        }
    }

    public static boolean routeExists(Route route) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedRoutes().forEach((s, r) -> {
            if (r.equals(route)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public static boolean routeExists(EndPoint p1, EndPoint p2) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedRoutes().forEach((s, r) -> {
            if (r.getInput().equals(p1) && r.getOutput().equals(p2)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }
}
