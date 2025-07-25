package host.plas.discord.data.channeling;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import host.plas.StreamlineDiscord;
import host.plas.discord.DiscordHandler;
import host.plas.discord.messaging.DiscordMessenger;
import host.plas.events.streamline.channels.ChanneledMessageEvent;
import singularity.data.console.CosmicSender;
import singularity.loading.Loadable;
import singularity.modules.ModuleUtils;
import singularity.utils.UserUtils;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter @Setter
public class Route implements Loadable<Route> {
    @Getter
    private static final File oldDataFolder = new File(StreamlineDiscord.getInstance().getDataFolder(), "routes" + File.separator);

    private String identifier;

    private EndPoint input;
    private EndPoint output;

    private boolean fullyLoaded = false;

    public Route(String identifier) {
        this.identifier = identifier;
    }

    public Route() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public void save(boolean async) {
        StreamlineDiscord.getRouteKeeper().save(this, async);
    }

    @Override
    public void load() {
        RouteLoader.registerRoute(this);
    }

    @Override
    public void unload() {
        RouteLoader.unregisterRoute(this);
    }

    @Override
    public boolean isLoaded() {
        return RouteLoader.getRoute(this.getIdentifier()).isPresent();
    }

    @Override
    public void saveAndUnload(boolean b) {
        save(b);
        unload();
    }

    public void bounceMessage(RoutedUser routedUser, String message) {
        ModuleUtils.fireEvent(new ChanneledMessageEvent(message, getInput(), getOutput()));

        switch (getOutput().getType()) {
            case GLOBAL_NATIVE:
                UserUtils.getOnlineSenders().forEach((s, user) -> {
                    ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                });
                break;
            case SPECIFIC_NATIVE:
                ModuleUtils.getOnlineUsers().forEach((s, user) -> {
                    if (user.getServer() != null) {
                        if (user.getServerName().equals(getOutput().getIdentifier())) {
                            ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                        }
                    }
                });
                break;
            case PERMISSION:
                UserUtils.getOnlineSenders().forEach((s, streamlineUser) -> {
                    if (ModuleUtils.hasPermission(streamlineUser, getOutput().getIdentifier())) ModuleUtils.sendMessage(streamlineUser,
                            getMutatedMessage(routedUser, message, getOutput()));
                });
                break;

//            case GUILD:
//                if (! DiscordModule.getConfig().allowDiscordToStreamlineGuilds()) return;
//                if (DiscordModule.getGroupsDependency().isPresent()) {
//                    DiscordModule.getGroupsDependency().getGuildMembersOf(getOutput().getIdentifier()).forEach((s, user) -> {
//                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
//                    });
//                }
//                break;
            case PARTY:
                if (! StreamlineDiscord.getConfig().allowDiscordToStreamlineParties()) return;
                if (StreamlineDiscord.getGroupsDependency().isPresent()) {
                    StreamlineDiscord.getGroupsDependency().getPartyMembersOf(getOutput().getIdentifier()).forEach((s, user) -> {
                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                    });
                }
                break;

//            case SPECIFIC_HANDLED:
//                if (! DiscordModule.getConfig().allowDiscordToStreamlineChannels()) return;
//                if (DiscordModule.getMessagingDependency().isPresent()) {
//                    DiscordModule.getMessagingDependency().getUsersInChannel(getOutput().getIdentifier()).forEach((s, user) -> {
//                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
//                    });
//                }
//                break;

            case DISCORD_TEXT:
                TextChannel channel = getOutput().asServerTextChannel();
                if (channel == null) return;

                if (isJsonFile(getOutput().getToFormat())) {
                    String fileName = getJsonFile(getOutput().getToFormat());

//                    CompletableFuture.runAsync(() -> {
//                        loadFile(fileName);
//                    }).join();
//
//                    DiscordMessenger.sendSimpleEmbed(channel.getIdLong(), ModuleUtils.stripColor(
//                            ModuleUtils.replaceAllPlayerBungee(routedUser.getUser(), getJsonFromFile(fileName)).replace("%this_message%", message)));
                } else {
                    DiscordMessenger.sendSimpleMessage(channel.getIdLong(), ModuleUtils.stripColor(
                            ModuleUtils.replaceAllPlayerBungee(routedUser.getUser(), getOutput().getToFormat().replace("%this_message%", message))));
                }
                break;
        }
    }

    public void bounceMessage(RoutedUser routedUser, String message, boolean forceEmbed) {
        if (forceEmbed) {
            TextChannel channel = getOutput().asServerTextChannel();
            if (channel == null) return;

            DiscordMessenger.sendSimpleEmbed(channel.getIdLong(), ModuleUtils.stripColor(
                    ModuleUtils.replaceAllPlayerBungee(routedUser.getUser(), message).replace("%this_message%", message)));
        } else {
            bounceMessage(routedUser, message);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route) {
            Route o = (Route) obj;
            return o.getInput().equals(getInput()) && o.getOutput().equals(getOutput());
        } else {
            return super.equals(obj);
        }
    }

    public String getMutatedMessage(RoutedUser user, String message, EndPoint endPoint) {
        if (! user.isDiscord()) return endPoint.getToFormat().replace("%this_message%", message);
        if (! StreamlineDiscord.getVerifiedUsers().isVerified(user.getDiscordId())) return endPoint.getToFormat()
                .replace("%streamline_user_absolute%",
                        DiscordHandler.getUser(user.getDiscordId()).getName() + "#" + DiscordHandler.getUser(user.getDiscordId()).getDiscriminator())
                .replace("%streamline_user_formatted%", DiscordHandler.getUser(user.getDiscordId()).getName())
                .replace("%this_message%", message);
        CosmicSender u = ModuleUtils.getOrCreateSender(StreamlineDiscord.getVerifiedUsers().getUUIDfromDiscordID(user.getDiscordId())).orElse(null);
        if (u == null) return endPoint.getToFormat()
                .replace("%streamline_user_absolute%",
                        DiscordHandler.getUser(user.getDiscordId()).getName() + "#" + DiscordHandler.getUser(user.getDiscordId()).getDiscriminator())
                .replace("%streamline_user_formatted%", DiscordHandler.getUser(user.getDiscordId()).getName())
                .replace("%this_message%", message);

        return ModuleUtils.replaceAllPlayerBungee(u, endPoint.getToFormat()).replace("%this_message%", message);
    }

    public void drop() {
        StreamlineDiscord.getRouteKeeper().drop(this);
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

    @Override
    public Route augment(CompletableFuture<Optional<Route>> completableFuture, boolean isGet) {
        this.fullyLoaded = false;

        completableFuture.whenComplete((optional, throwable) -> {
            if (throwable != null) {
                StreamlineDiscord.getInstance().logWarning("An error occurred while loading the route: " + this.getIdentifier());
                StreamlineDiscord.getInstance().logWarning(throwable.getStackTrace());
                return;
            }

            if (optional.isPresent()) {
                Route route = optional.get();

                this.setIdentifier(route.getIdentifier());
                this.setInput(route.getInput());
                this.setOutput(route.getOutput());
            } else {
                if (! isGet) {
                    save();
                }
            }

            this.fullyLoaded = true;
        });

        return this;
    }
}
