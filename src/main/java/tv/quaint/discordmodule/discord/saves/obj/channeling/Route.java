package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.StorageUtils;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.events.ChanneledMessageEvent;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Route extends SavableResource {
    @Getter
    private static final File dataFolder = new File(DiscordModule.getInstance().getDataFolder(), "routes" + File.separator);

    @Getter @Setter
    private EndPoint input;
    @Getter @Setter
    private EndPoint output;

    public Route(String uuid) {
        super(uuid, StorageUtils.newStorageResource(uuid, Route.class, StorageUtils.StorageType.YAML, getDataFolder(), null));
    }

    public Route(EndPoint input, EndPoint output) {
        this(UUID.randomUUID().toString());

        setInput(input);
        setOutput(output);

        saveAll();
    }

    @Override
    public void populateDefaults() {
        EndPointType inputType = EndPointType.valueOf(getStorageResource().getOrSetDefault("input.type", "GLOBAL_NATIVE"));
        String inputIdentifier = getStorageResource().getOrSetDefault("input.identifier", "null");
        String inputFormat = getStorageResource().getOrSetDefault("input.format", "null");
        EndPointType outputType = EndPointType.valueOf(getStorageResource().getOrSetDefault("output.type", "GLOBAL_NATIVE"));
        String outputIdentifier = getStorageResource().getOrSetDefault("output.identifier", "null");
        String outputFormat = getStorageResource().getOrSetDefault("output.format", "null");

        setInput(new EndPoint(inputType, inputIdentifier, inputFormat));
        setOutput(new EndPoint(outputType, outputIdentifier, outputFormat));
    }

    @Override
    public void loadValues() {
        EndPointType inputType = EndPointType.valueOf(getStorageResource().getOrSetDefault("input.type", "GLOBAL_NATIVE"));
        String inputIdentifier = getStorageResource().getOrSetDefault("input.identifier", "null");
        String inputFormat = getStorageResource().getOrSetDefault("input.format", "null");
        EndPointType outputType = EndPointType.valueOf(getStorageResource().getOrSetDefault("output.type", "GLOBAL_NATIVE"));
        String outputIdentifier = getStorageResource().getOrSetDefault("output.identifier", "null");
        String outputFormat = getStorageResource().getOrSetDefault("output.format", "null");

        setInput(new EndPoint(inputType, inputIdentifier, inputFormat));
        setOutput(new EndPoint(outputType, outputIdentifier, outputFormat));
    }

    @Override
    public void saveAll() {
        getStorageResource().write("input.type", getInput().getType());
        getStorageResource().write("input.identifier", getInput().getIdentifier());
        getStorageResource().write("input.format", getInput().getToFormat());
        getStorageResource().write("output.type", getOutput().getType());
        getStorageResource().write("output.identifier", getOutput().getIdentifier());
        getStorageResource().write("output.format", getOutput().getToFormat());
    }

    public void bounceMessage(RoutedUser routedUser, String message) {
        ModuleUtils.fireEvent(new ChanneledMessageEvent(message, getInput(), getOutput()));

        switch (getOutput().getType()) {
            case GLOBAL_NATIVE -> {
                UserUtils.getOnlineUsers().forEach((s, user) -> {
                    ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                });
            }
            case SPECIFIC_NATIVE -> {
                UserUtils.getPlayersOn(getOutput().getIdentifier()).forEach(streamlinePlayer -> {
                    ModuleUtils.sendMessage(streamlinePlayer, getMutatedMessage(routedUser, message, getOutput()));
                });
            }
            case PERMISSION -> {
                UserUtils.getOnlineUsers().forEach((s, streamlineUser) -> {
                    if (ModuleUtils.hasPermission(streamlineUser, getOutput().getIdentifier())) ModuleUtils.sendMessage(streamlineUser,
                            getMutatedMessage(routedUser, message, getOutput()));
                });
            }

            case GUILD -> {
                if (! DiscordModule.getConfig().allowDiscordToStreamlineGuilds()) return;
                if (DiscordModule.getGroupsDependency().isPresent()) {
                    DiscordModule.getGroupsDependency().getGuildMembersOf(getOutput().getIdentifier()).forEach((s, user) -> {
                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                    });
                }
            }
            case PARTY -> {
                if (! DiscordModule.getConfig().allowDiscordToStreamlineParties()) return;
                if (DiscordModule.getGroupsDependency().isPresent()) {
                    DiscordModule.getGroupsDependency().getPartyMembersOf(getOutput().getIdentifier()).forEach((s, user) -> {
                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                    });
                }
            }

            case SPECIFIC_HANDLED -> {
                if (! DiscordModule.getConfig().allowDiscordToStreamlineChannels()) return;
                if (DiscordModule.getMessagingDependency().isPresent()) {
                    DiscordModule.getMessagingDependency().getUsersInChannel(getOutput().getIdentifier()).forEach((s, user) -> {
                        ModuleUtils.sendMessage(user, getMutatedMessage(routedUser, message, getOutput()));
                    });
                }
            }

            case DISCORD_TEXT -> {
                Optional<ServerTextChannel> channelOptional = getOutput().asServerTextChannel();
                if (channelOptional.isEmpty()) return;
                ServerTextChannel channel = channelOptional.get();

                if (DiscordModule.isJsonFile(getOutput().getToFormat())) {
                    String fileName = DiscordModule.getJsonFile(getOutput().getToFormat());

                    CompletableFuture.runAsync(() -> {
                        DiscordModule.loadFile(fileName);
                    }).join();

                    DiscordMessenger.sendSimpleEmbed(channel.getId(), ModuleUtils.replaceAllPlayerBungee(routedUser.getUser(), DiscordModule.getJsonFromFile(fileName)).replace("%this_message%", message));
                } else {
                    DiscordMessenger.sendMessage(channel.getId(), ModuleUtils.replaceAllPlayerBungee(routedUser.getUser(), getOutput().getToFormat().replace("%this_message%", message)));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route o) {
            return o.getInput().equals(getInput()) && o.getOutput().equals(getOutput());
        } else {
            return super.equals(obj);
        }
    }

    public String getMutatedMessage(RoutedUser user, String message, EndPoint endPoint) {
        if (! user.isDiscord()) return endPoint.getToFormat().replace("%this_message%", message);
        if (! DiscordModule.getVerifiedUsers().isVerified(user.getDiscordId())) return endPoint.getToFormat()
                .replace("%streamline_user_absolute%", DiscordHandler.getUser(user.getDiscordId()).getDiscriminatedName())
                .replace("%streamline_user_formatted%", DiscordHandler.getUser(user.getDiscordId()).getName())
                .replace("%this_message%", message);
        StreamlineUser u = ModuleUtils.getOrGetUser(DiscordModule.getVerifiedUsers().discordIdToUUID(user.getDiscordId()));
        if (u == null) return endPoint.getToFormat()
                .replace("%streamline_user_absolute%", DiscordHandler.getUser(user.getDiscordId()).getDiscriminatedName())
                .replace("%streamline_user_formatted%", DiscordHandler.getUser(user.getDiscordId()).getName())
                .replace("%this_message%", message);

        return ModuleUtils.replaceAllPlayerBungee(u, endPoint.getToFormat()).replace("%this_message%", message);
    }

    public void remove() {
        DiscordHandler.unloadRoute(getUuid());

        try {
            getStorageResource().delete();
            dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
