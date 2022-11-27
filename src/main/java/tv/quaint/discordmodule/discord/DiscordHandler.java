package tv.quaint.discordmodule.discord;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.streamline.api.SLAPI;
import net.streamline.api.interfaces.IStreamline;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.commands.*;
import tv.quaint.discordmodule.discord.saves.obj.BotLayout;
import tv.quaint.discordmodule.discord.saves.obj.channeling.*;
import tv.quaint.discordmodule.discord.voice.StreamlineVoiceInterceptor;
import tv.quaint.discordmodule.events.BotReadyEvent;
import tv.quaint.discordmodule.events.BotStoppedEvent;
import tv.quaint.discordmodule.server.ServerEvent;
import tv.quaint.discordmodule.server.events.spigot.SpigotEventManager;
import tv.quaint.discordmodule.server.events.streamline.LoginDSLEvent;
import tv.quaint.discordmodule.server.events.streamline.LogoutDSLEvent;
import tv.quaint.storage.StorageUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordHandler {
    @Getter
    private static final File forwardedJsonsFolder = new File(DiscordModule.getInstance().getDataFolder(), "forwarded-jsons" + File.separator);

    @Getter @Setter
    private static AtomicReference<JDA> concurrentDiscordAPI;

    public static JDA getDiscordAPI() {
        if (getConcurrentDiscordAPI() == null) return null;
        return getConcurrentDiscordAPI().get();
    }

    public static void setDiscordAPI(JDA value) {
        if (getConcurrentDiscordAPI() == null) {
            setConcurrentDiscordAPI(new AtomicReference<>(value));
            return;
        }
        getConcurrentDiscordAPI().set(value);
    }

    @NonNull
    public static JDA safeDiscordAPI() {
        return Objects.requireNonNull(getDiscordAPI(), "The Concurrent DiscordAPI is 'null'!");
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, DiscordCommand> registeredCommands = new ConcurrentSkipListMap<>();

    public static long getBotId() {
        return getBotUser().getIdLong();
    }

    public static User getBotUser() {
        return safeDiscordAPI().getSelfUser();
    }

    public static long getUserId(User user) {
        return user.getIdLong();
    }

    public static User getUser(long userId) {
        return safeDiscordAPI().getUserById(userId);
    }

    public static void updateBotAvatar(String url) {
        try {
            safeDiscordAPI().getSelfUser().getManager().setAvatar(Icon.from(new URL(url).openStream()));
        } catch (Exception e) {
            DiscordModule.getInstance().logWarning("Couldn't change the bot's avatar due to...");
            DiscordModule.getInstance().logWarning(e.getStackTrace());
        }
    }

    public static ConcurrentSkipListMap<Long, Guild> getJoinedServers() {
        ConcurrentSkipListMap<Long, Guild> r = new ConcurrentSkipListMap<>();

        for (Guild server : DiscordHandler.safeDiscordAPI().getSelfUser().getMutualGuilds()) {
            r.put(server.getIdLong(), server);
        }

        return r;
    }

    public static Guild getServerById(long id) {
        return getJoinedServers().get(id);
    }

    public static TextChannel getTextChannelById(long id) {
        return safeDiscordAPI().getTextChannelById(id);
    }

    public static VoiceChannel getVoiceChannelById(long id) {
        return safeDiscordAPI().getVoiceChannelById(id);
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
        StorageUtils.ensureFileFromSelf(DiscordModule.getInstance().getWrapper().getPluginClassLoader(),
                getForwardedJsonsFolder(), new File(getForwardedJsonsFolder(), "on-login.json"), "on-login.json");
        StorageUtils.ensureFileFromSelf(DiscordModule.getInstance().getWrapper().getPluginClassLoader(),
                getForwardedJsonsFolder(), new File(getForwardedJsonsFolder(), "on-logout.json"), "on-logout.json");
        StorageUtils.ensureFileFromSelf(DiscordModule.getInstance().getWrapper().getPluginClassLoader(),
                getForwardedJsonsFolder(), new File(getForwardedJsonsFolder(), "on-advancement.json"), "on-advancement.json");
        StorageUtils.ensureFileFromSelf(DiscordModule.getInstance().getWrapper().getPluginClassLoader(),
                getForwardedJsonsFolder(), new File(getForwardedJsonsFolder(), "on-death.json"), "on-death.json");

        return CompletableFuture.supplyAsync(() -> {
            kill().completeOnTimeout(false, 7, TimeUnit.SECONDS).join();

            if ((! isBackEnd() || (! DiscordModule.getConfig().moduleForwardsEventsToProxy())) && ! DiscordModule.getConfig().fullDisable()) {
                DiscordModule.getInstance().logInfo("Bot is initializing...!");

                BotLayout layout = DiscordModule.getConfig().getBotLayout();
                try {
                    setDiscordAPI(JDABuilder.createDefault(
                                    layout.getToken(),
                                    List.of(GatewayIntent.values())
                            )
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .setVoiceDispatchInterceptor(new StreamlineVoiceInterceptor())
                            .setActivity(Activity.of(layout.getActivityType(), layout.getActivityValue()))
                            .build()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                CompletableFuture.supplyAsync(() -> {
                    JDA.Status status = safeDiscordAPI().getStatus();
                    while (! status.equals(JDA.Status.CONNECTED)) {
                        status = safeDiscordAPI().getStatus();
                    }
                    return true;
                }).completeOnTimeout(false, 12, TimeUnit.SECONDS).join();

                safeDiscordAPI().addEventListener(new DiscordListener());

                updateBotAvatar(layout.getAvatarUrl());
            }

            registerCommands();

            initServerEvents();

            fixOldRoutes();

            loadAllChanneledFolders(false);

            if (getDiscordAPI() != null) DiscordModule.getInstance().logInfo("Initialized!");

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
                safeDiscordAPI().shutdownNow();
            }

            setConcurrentDiscordAPI(null);

            new BotStoppedEvent().fire();

            return true;
        });
    }

    public static void registerCommand(DiscordCommand command) {
        getRegisteredCommands().put(command.getCommandIdentifier(), command);
        DiscordModule.getInstance().logInfo("Registered DiscordCommand '" + command.getCommandIdentifier() + "'.");
    }

    public static void unregisterCommand(String identifier) {
        getRegisteredCommands().remove(identifier);
        DiscordModule.getInstance().logInfo("Unregistered DiscordCommand '" + identifier + "'.");
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
        DiscordModule.getInstance().logInfo("Registered &cServerEvent &rwith identifier '&d" + event.getIdentifier() + "&r'.");
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

    public static ConcurrentSkipListSet<Route> getAllCurrentRoutes(StreamlineUser player) {
        ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>();

        getLoadedChanneledFolders().forEach((s, folder) -> {
            folder.getLoadedRoutes().forEach((st, route) -> {
                if (route.getInput().getType() == EndPointType.GLOBAL_NATIVE) routes.add(route);
                if (route.getInput().getType() == EndPointType.SPECIFIC_NATIVE) {
                    if (route.getInput().getIdentifier().equals(player.getLatestServer())) routes.add(route);
                }
                if (route.getInput().getType() == EndPointType.GUILD) {
                    if (DiscordModule.getGroupsDependency().isPresent()) {
                        if (DiscordModule.getGroupsDependency().getGuildMembersOf(route.getInput().getIdentifier()).containsKey(player.getUuid()))
                            routes.add(route);
                    }
                }
                if (route.getInput().getType() == EndPointType.PARTY) {
                    if (DiscordModule.getGroupsDependency().isPresent()) {
                        if (DiscordModule.getGroupsDependency().getPartyMembersOf(route.getInput().getIdentifier()).containsKey(player.getUuid()))
                            routes.add(route);
                    }
                }
            });
        });

        return routes;
    }

    public static ConcurrentSkipListSet<String> allEndPointTypesAsStrings() {
        ConcurrentSkipListSet<String> strings = new ConcurrentSkipListSet<>();
        for (EndPointType type : EndPointType.values()) strings.add(type.toString());
        return strings;
    }
}
