package tv.quaint.discordmodule.discord;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.interfaces.IStreamline;
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
import tv.quaint.discordmodule.discord.commands.*;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.discord.saves.obj.channeling.*;
import tv.quaint.discordmodule.events.DiscordMessageEvent;
import tv.quaint.discordmodule.server.ServerEvent;
import tv.quaint.discordmodule.server.events.spigot.SpigotEventManager;
import tv.quaint.discordmodule.server.events.streamline.LoginDSLEvent;
import tv.quaint.discordmodule.server.events.streamline.LogoutDSLEvent;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordHandler {
    @Getter
    private static final File forwardedJsonsFolder = new File(DiscordModule.getInstance().getDataFolder(), "forwarded-jsons" + File.separator);

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

    public static void updateBotAvatar(String url) {
        try {
            safeDiscordAPI().updateAvatar(new URL(url)).join();
        } catch (Exception e) {
            DiscordModule.getInstance().logWarning("Couldn't change the bot's avatar due to...");
            DiscordModule.getInstance().logWarning(e.getStackTrace());
        }
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
        new HelpCommand();
    }

    public static CompletableFuture<Boolean> init() {
        getForwardedJsonsFolder().mkdirs();

        return CompletableFuture.supplyAsync(() -> {
            kill().join();

            if (! isBackEnd() || ! DiscordModule.getConfig().moduleForwardsEventsToProxy()) {
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

                    if (optionalUser.get().isBot()) DiscordMessenger.incrementMessageCountInBots();
                    else DiscordMessenger.incrementMessageCountIn();

                    ModuleUtils.fireEvent(new DiscordMessageEvent(new MessagedString(optionalUser.get(), e.getMessageAuthor(), e.getChannel(), e.getMessageContent())));
                });

                safeDiscordAPI().updateActivity(layout.getActivityType(), layout.getActivityValue());

                updateBotAvatar(layout.getAvatarUrl());
            }

            registerCommands();

            initServerEvents();

            fixOldRoutes();

            loadAllChanneledFolders(false);

            if (getDiscordAPI() != null) DiscordModule.getInstance().logInfo("Bot is initialized!");

            return true;
        });
    }

    public static CompletableFuture<Boolean> kill() {
        return CompletableFuture.supplyAsync(() -> {
            if (getDiscordAPI() == null) return false;

            getRegisteredCommands().forEach((s, command) -> {
                command.unregister();
            });

            getLoadedChanneledFolders().forEach((s, folder) -> {
                folder.killRoutes();
                folder.killEventRoutes();
            });

            killServerEvents();

            if (! DiscordModule.getConfig().moduleForwardsEventsToProxy()) {
                safeDiscordAPI().disconnect().join();
            }

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

        getPendingVerifications().forEach((uuid, s) -> {
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

    public static boolean verifyUser(long discordId, String verification) {
        if (DiscordModule.getVerifiedUsers().isVerified(discordId)) return false;
        if (!hasVerification(verification)) return false;
        StreamlineUser user = getPendingVerificationUser(verification);
        DiscordModule.getVerifiedUsers().verifyUser(user.getUuid(), discordId);
        getPendingVerifications().remove(user.getUuid());
        return true;
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, ChanneledFolder> loadedChanneledFolders = new ConcurrentSkipListMap<>();

    public static void loadChanneledFolder(ChanneledFolder folder) {
        if (channeledFolderExists(folder.getIdentifier())) return;
        getLoadedChanneledFolders().put(folder.getIdentifier(), folder);
        folder.loadAllRoutes();
        folder.loadAllEventRoutes();
        DiscordModule.getInstance().logInfo("Loaded ChanneledFolder '" + folder.getIdentifier() + "'!");
    }

    public static void unloadChanneledFolder(String identifier) {
        getLoadedChanneledFolders().remove(identifier);
    }

    public static void killChannelFolders() {
        getLoadedChanneledFolders().forEach((s, f) -> {
            f.getLoadedRoutes().forEach((st, route) -> route.saveAll());
            f.setLoadedRoutes(new ConcurrentSkipListMap<>());
        });
        setLoadedChanneledFolders(new ConcurrentSkipListMap<>());
    }

    public static void loadAllChanneledFolders(boolean kill) {
        if (kill) killChannelFolders();

        File[] files = ChanneledFolder.getDataFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.isDirectory()) return;

            String identifier = file.getName();
            loadChanneledFolder(new ChanneledFolder(identifier));
        }
    }

    public static boolean channeledFolderExists(ChanneledFolder folder) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedChanneledFolders().forEach((s, f) -> {
            if (f.getIdentifier().equals(folder.getIdentifier())) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public static boolean channeledFolderExists(String identifier) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        getLoadedChanneledFolders().forEach((s, f) -> {
            if (f.getIdentifier().equals(identifier)) atomicBoolean.set(true);
        });

        return atomicBoolean.get();
    }

    public static ChanneledFolder getChanneledFolderFromAncientRoute(Route route) {
        if (channeledFolderExists(route.getOutput().getType() + "-" + route.getOutput().getIdentifier())) {
            return getLoadedChanneledFolders().get(route.getOutput().getType() + "-" + route.getOutput().getIdentifier());
        }

        return new ChanneledFolder(route.getOutput().getType() + "-" + route.getOutput().getIdentifier());
    }

    public static ChanneledFolder getChanneledFolderFromRoute(Route route) {
        return route.getParent();
    }

    public static ChanneledFolder getChanneledFolderFromEventRoute(ServerEventRoute<?> eventRoute) {
        return eventRoute.getParent();
    }

    public static void fixOldRoutes() {
        File folder = Route.getOldDataFolder();
        if (! folder.exists()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.isFile()) return;
            if (! file.getName().endsWith(".yml")) return;

            String uuid = file.getName().substring(0, file.getName().lastIndexOf("."));
            Route route = new Route(uuid);
            if (! makeRouteNew(route)) DiscordModule.getInstance().logWarning("Could not transfer a route with a UUID of '" + uuid + "'.");
        }
        folder.delete();
    }

    public static boolean makeRouteNew(Route ancientRoute) {
        if (ancientRoute == null) return false;

        ChanneledFolder folder = getChanneledFolderFromAncientRoute(ancientRoute);
        Route newRoute = new Route(ancientRoute.getUuid(), folder);
        boolean bool = folder.loadRoute(newRoute);
        ancientRoute.remove();
        return bool;
    }

    public static void pollAllChanneledFolders() {
        loadAllChanneledFolders(true);
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, ServerEvent<?>> registeredEvents = new ConcurrentSkipListMap<>();

    public static void registerServerEvent(ServerEvent<?> event) {
        getRegisteredEvents().put(event.getIdentifier(), event);
    }

    public static void unregisterServerEvent(String identifier) {
        getRegisteredEvents().remove(identifier);
    }

    public static void unregisterServerEvent(ServerEvent<?> event) {
        unregisterServerEvent(event.getIdentifier());
    }

    public static void initServerEvents() {
        if (DiscordModule.getConfig().serverEventStreamlineLogin()) registerServerEvent(new LoginDSLEvent());
        if (DiscordModule.getConfig().serverEventStreamlineLogout()) registerServerEvent(new LogoutDSLEvent());

        if (isBackEnd()) {
            SpigotEventManager.loadAllSpigot();
        }
    }

    public static void killServerEvents() {
        if (DiscordModule.getConfig().serverEventStreamlineLogin()) unregisterServerEvent("login");
        if (DiscordModule.getConfig().serverEventStreamlineLogout()) unregisterServerEvent("logout");

        if (isBackEnd()) {
            SpigotEventManager.unloadAllSpigot();
        }
    }

    public static ServerEvent<?> getServerEvent(String identifier) {
        return getRegisteredEvents().get(identifier);
    }

    public static <T extends ServerEvent<?>> T getServerEvent(Class<T> clazz) {
        AtomicReference<T> r = new AtomicReference<>(null);

        getRegisteredEvents().forEach((s, event) -> {
            if (event.getClass().isAssignableFrom(clazz)) r.set((T) event);
        });

        return r.get();
    }

    public static boolean containsServerEvent(String identifier) {
        return getRegisteredEvents().containsKey(identifier);
    }

    public static boolean isBackEnd() {
        return SLAPI.getInstance().getPlatform().getServerType().equals(IStreamline.ServerType.BACKEND);
    }
}
