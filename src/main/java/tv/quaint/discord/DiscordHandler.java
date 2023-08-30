package tv.quaint.discord;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.SLAPI;
import net.streamline.api.interfaces.IStreamline;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import tv.quaint.DiscordModule;
import tv.quaint.discord.commands.*;
import tv.quaint.discord.saves.obj.channeling.ChanneledFolder;
import tv.quaint.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discord.saves.obj.channeling.Route;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;
import tv.quaint.discord.saves.obj.BotLayout;
import tv.quaint.discord.voice.StreamlineVoiceInterceptor;
import tv.quaint.events.BotStoppedEvent;
import tv.quaint.events.verification.on.VerificationAlreadyVerifiedEvent;
import tv.quaint.events.verification.on.VerificationFailureEvent;
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
        new VerifyCommand();
        new UnVerifyCommand();
    }

    public static CompletableFuture<Boolean> init() {
        getForwardedJsonsFolder().mkdirs();

        return CompletableFuture.supplyAsync(() -> {
            kill().completeOnTimeout(false, 7, TimeUnit.SECONDS).join();

            if (! DiscordModule.getConfig().fullDisable()) {
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
                }).completeOnTimeout(false, 1777, TimeUnit.MILLISECONDS).join();

                safeDiscordAPI().addEventListener(new DiscordListener());

                updateBotAvatar(layout.getAvatarUrl());

                registerCommands();
            }

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
            });

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

    public static SingleSet<MessageCreateData, BotMessageConfig> tryVerificationForUser(MessagedString messagedString, String verification, boolean fromCommand) {
        if (DiscordModule.getVerifiedUsers().isVerified(messagedString.getAuthor().getIdLong())) {
            new VerificationAlreadyVerifiedEvent(messagedString, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().verifiedFailureAlreadyVerifiedDiscord());
        }
        if (! hasVerification(verification)) {
            new VerificationFailureEvent(messagedString, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().verifiedFailureGenericDiscord());
        }
        StreamlineUser user = getPendingVerificationUser(verification);
        SingleSet<MessageCreateData, BotMessageConfig> data = DiscordModule.getVerifiedUsers().verifyUser(user.getUuid(), messagedString, verification, fromCommand);
        getPendingVerifications().remove(user.getUuid());
        return data;
    }

    @Getter @Setter
    private static ConcurrentSkipListMap<String, ChanneledFolder> loadedChanneledFolders = new ConcurrentSkipListMap<>();

    public static void loadChanneledFolder(ChanneledFolder folder) {
        if (channeledFolderExists(folder.getIdentifier())) return;
        getLoadedChanneledFolders().put(folder.getIdentifier(), folder);
        folder.loadAllRoutes();
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

    @Getter @Setter
    private static ConcurrentSkipListMap<DiscordCommand, Long> registeredSlashCommands = new ConcurrentSkipListMap<>();

    public static void registerSlashCommand(DiscordCommand discordCommand) {
        if (getRegisteredSlashCommands().containsValue(discordCommand.getSlashCommandSnowflake())) {
            DiscordModule.getInstance().logWarning("Could not register slash command with identifier '" + discordCommand.getCommandIdentifier() + "' because it is already registered.");
            return;
        }

        Command command = discordCommand.setupOptionData(safeDiscordAPI().upsertCommand(discordCommand.getCommandIdentifier(), discordCommand.getDescription())).complete();

        getRegisteredSlashCommands().put(discordCommand, command.getIdLong());
        discordCommand.setSlashCommandSnowflake(command.getIdLong());

        DiscordModule.getInstance().logInfo("Registered &cDiscordCommand &rwith identifier '&d" + discordCommand.getCommandIdentifier() + "&r'.");
    }

    public static void unregisterSlashCommand(DiscordCommand discordCommand) {
        if (! getRegisteredSlashCommands().containsValue(discordCommand.getSlashCommandSnowflake())) {
            DiscordModule.getInstance().logWarning("Could not register slash command with identifier '" + discordCommand.getCommandIdentifier() + "' because it is already unregistered.");
            return;
        }

        safeDiscordAPI().deleteCommandById(discordCommand.getSlashCommandSnowflake()).queue();
        discordCommand.setSlashCommandSnowflake(-1);

        getRegisteredSlashCommands().remove(discordCommand);
        DiscordModule.getInstance().logInfo("Unregistered &cDiscordCommand &rwith identifier '&d" + discordCommand.getCommandIdentifier() + "&r'.");
    }

    public static DiscordCommand getSlashCommand(long identifier) {
        AtomicReference<DiscordCommand> commandAtomicReference = new AtomicReference<>(null);

        getRegisteredSlashCommands().forEach((command, aLong) -> {
            if (aLong == identifier) commandAtomicReference.set(command);
        });

        return commandAtomicReference.get();
    }
}
