package tv.quaint.discordmodule.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.BotMessageConfig;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.channeling.*;
import tv.quaint.discordmodule.server.events.spigot.SpigotEventManager;
import tv.quaint.discordmodule.server.events.streamline.LoginDSLEvent;
import tv.quaint.discordmodule.server.events.streamline.LogoutDSLEvent;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessageSet;
    @Getter @Setter
    private String replyMessageRemove;
    @Getter @Setter
    private String replyMessageInfo;
    @Getter @Setter
    private String replyMessageNone;

    public ChannelCommand() {
        super("channel",
                -1L,
                "chan", "ch", "channel"
        );

        setReplyMessageSet(getResource().getOrSetDefault("messages.reply.set", "--file:channel-set-response.json"));
        setReplyMessageRemove(getResource().getOrSetDefault("messages.reply.remove", "--file:channel-remove-response.json"));
        setReplyMessageInfo(getResource().getOrSetDefault("messages.reply.info", "--file:channel-info-response.json"));
        setReplyMessageNone(getResource().getOrSetDefault("messages.reply.none", "--file:channel-none-response.json"));
        loadFile("channel-set-response.json");
        loadFile("channel-remove-response.json");
        loadFile("channel-info-response.json");
        loadFile("channel-none-response.json");
    }

    @Override
    public SingleSet<MessageCreateData, BotMessageConfig> executeMore(MessagedString messagedString) {
        if (! messagedString.hasCommandArgs()) {
            return messageInfo(messagedString);
        }

        String action = messagedString.getCommandArgs()[0].toLowerCase();
        switch (action) {
            case "set" -> {
                if (messagedString.getCommandArgs().length < 2) {
                    return messageInfo(messagedString);
                }

                EndPointType type;

                try {
                    type = EndPointType.valueOf(messagedString.getCommandArgs()[1].toUpperCase());
                } catch (Exception e) {
                    return messageInfo(messagedString);
                }

                if (type.equals(EndPointType.GLOBAL_NATIVE) && messagedString.getCommandArgs().length == 2) {
                    EndPoint discord = new EndPoint(EndPointType.DISCORD_TEXT,
                            messagedString.getChannel().getId(), DiscordModule.getConfig().getDefaultFormatFromMinecraft());
                    EndPoint other = new EndPoint(EndPointType.GLOBAL_NATIVE, "", DiscordModule.getConfig().getDefaultFormatFromDiscord());
                    ChanneledFolder folder = new ChanneledFolder(discord.getType() + "-" + discord.getIdentifier());
                    Route toDiscord = new Route(other, discord, folder);
                    Route toOther = new Route(discord, other, folder);

                    if (DiscordModule.getConfig().serverEventAllEventsOnDiscordRoute()) {
                        if (DiscordHandler.containsServerEvent("login")) {
                            ServerEventRoute<LoginDSLEvent> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(LoginDSLEvent.class));
                            folder.loadEventRoute(r);
                        }
                        if (DiscordHandler.containsServerEvent("logout")) {
                            ServerEventRoute<LogoutDSLEvent> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(LogoutDSLEvent.class));
                            folder.loadEventRoute(r);
                        }
                        if (DiscordHandler.isBackEnd()) {
                            SpigotEventManager.addAdvancementEvent(other, folder);
                            SpigotEventManager.addDeathEvent(other, folder);
                        }
                    }

                    folder.loadRoute(toDiscord);
                    folder.loadRoute(toOther);
                    DiscordHandler.loadChanneledFolder(folder);

                    DiscordHandler.pollAllChanneledFolders();

                    return messageSet(messagedString, other);
                }

                if (messagedString.getCommandArgs().length < 3) {
                    return messageInfo(messagedString);
                }

                String otherFormat = type.equals(EndPointType.DISCORD_TEXT) ? DiscordModule.getConfig().getDefaultFormatFromDiscord()
                        : DiscordModule.getConfig().getDefaultFormatFromMinecraft();
                String discordFormat = DiscordModule.getConfig().getDefaultFormatFromDiscord();

                if (messagedString.getCommandArgs().length >= 4) {
                    otherFormat = ModuleUtils.argsToStringMinus(messagedString.getCommandArgs(), 0, 1, 2);
                }

                EndPoint discord = new EndPoint(EndPointType.DISCORD_TEXT,
                        messagedString.getChannel().getId(), otherFormat);
                EndPoint other = new EndPoint(type, messagedString.getCommandArgs()[2], discordFormat);
                ChanneledFolder folder = new ChanneledFolder(discord.getType() + "-" + discord.getIdentifier());
                Route toDiscord = new Route(other, discord, folder);
                Route toOther = new Route(discord, other, folder);

                if (DiscordModule.getConfig().serverEventAllEventsOnDiscordRoute()) {
                    if (DiscordHandler.containsServerEvent("login")) {
                        ServerEventRoute<LoginDSLEvent> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(LoginDSLEvent.class));
                        folder.loadEventRoute(r);
                    }
                    if (DiscordHandler.containsServerEvent("logout")) {
                        ServerEventRoute<LogoutDSLEvent> r = new ServerEventRoute<>(other, folder, DiscordHandler.getServerEvent(LogoutDSLEvent.class));
                        folder.loadEventRoute(r);
                    }
                    if (DiscordHandler.isBackEnd()) {
                        SpigotEventManager.addAdvancementEvent(other, folder);
                        SpigotEventManager.addDeathEvent(other, folder);
                    }
                }

                folder.loadRoute(toDiscord);
                folder.loadRoute(toOther);
                DiscordHandler.loadChanneledFolder(folder);

                DiscordHandler.pollAllChanneledFolders();

                return messageSet(messagedString, other);
            }
            case "remove" -> {
                if (messagedString.getCommandArgs().length == 1) {
                    AtomicReference<SingleSet<MessageCreateData, BotMessageConfig>> data = new AtomicReference<>(DiscordMessenger.simpleMessage("No channel found to remove!"));
                    DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                        folder.getAssociatedRoutes(EndPointType.DISCORD_TEXT, messagedString.getChannel().getId()).forEach(route -> {
                            data.set(messageRemove(messagedString, route.getOutput()));
                            route.remove();
                        });

                        DiscordHandler.pollAllChanneledFolders();
                    });
                    return data.get();
                }

                if (messagedString.getCommandArgs().length < 3) {
                    return messageInfo(messagedString);
                }

                EndPointType type;

                try {
                    type = EndPointType.valueOf(messagedString.getCommandArgs()[1].toUpperCase());
                } catch (Exception e) {
                    return messageInfo(messagedString);
                }

                String identifier = messagedString.getCommandArgs()[2];

                ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>();
                DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                    routes.addAll(folder.getBackAndForthRoute(type, identifier, messagedString.getChannel().getId()));
                });

                Optional<Route> thing = routes.stream().filter(route -> ! route.getInput().getType().equals(EndPointType.DISCORD_TEXT)).findFirst();

                if (thing.isEmpty()) {
                    return messageNone(messagedString);
                }

                EndPoint point = thing.get().getOutput();

                SingleSet<MessageCreateData, BotMessageConfig> data = messageRemove(messagedString, point);

                routes.forEach(Route::remove);

                DiscordHandler.pollAllChanneledFolders();

                return data;
            }
            default -> {
                return messageInfo(messagedString);
            }
        }
    }

    public SingleSet<MessageCreateData, BotMessageConfig> messageSet(MessagedString messagedString, EndPoint endPoint) {
        return message(messagedString, getReplyMessageSet(), endPoint);
    }

    public SingleSet<MessageCreateData, BotMessageConfig> messageRemove(MessagedString messagedString, EndPoint endPoint) {
        return message(messagedString, getReplyMessageRemove(), endPoint);
    }

    public SingleSet<MessageCreateData, BotMessageConfig> messageInfo(MessagedString messagedString) {
        return message(messagedString, getReplyMessageInfo());
    }

    public SingleSet<MessageCreateData, BotMessageConfig> messageNone(MessagedString messagedString) {
        return message(messagedString, getReplyMessageNone());
    }

    public SingleSet<MessageCreateData, BotMessageConfig> message(MessagedString messagedString, String message) {
        if (isJsonFile(message)) {
            String json = getJsonFromFile(getJsonFile(message));
            return DiscordMessenger.simpleEmbed(ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getId())
            ));
        } else {
            return DiscordMessenger.simpleMessage(message
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getId())
            );
        }
    }

    public SingleSet<MessageCreateData, BotMessageConfig> message(MessagedString messagedString, String message, EndPoint endPoint) {
        if (isJsonFile(message)) {
            String json = getJsonFromFile(getJsonFile(message));
            return DiscordMessenger.simpleEmbed(
                    ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(),
                            json
                                    .replace("%this_command_label%", messagedString.getBase())
                                    .replace("%this_channel_id%", messagedString.getChannel().getId())
                                    .replace("%this_type%", endPoint.getType().toString())
                                    .replace("%this_identifier%", endPoint.getIdentifier())
                                    .replace("%this_format%", endPoint.getToFormat())
                    )
            );
        } else {
            return DiscordMessenger.simpleMessage(message
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getId())
                    .replace("%this_type%", endPoint.getType().toString())
                    .replace("%this_identifier%", endPoint.getIdentifier())
                    .replace("%this_format%", endPoint.getToFormat())
            );
        }
    }

    @Override
    public void init() {

    }
}
