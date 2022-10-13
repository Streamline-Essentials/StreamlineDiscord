package tv.quaint.discordmodule.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPoint;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

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
                "chan", "ch"
        );

        setReplyMessageSet(resource.getOrSetDefault("messages.reply.set", "--file:channel-set-response.json"));
        setReplyMessageRemove(resource.getOrSetDefault("messages.reply.remove", "--file:channel-remove-response.json"));
        setReplyMessageInfo(resource.getOrSetDefault("messages.reply.info", "--file:channel-info-response.json"));
        setReplyMessageNone(resource.getOrSetDefault("messages.reply.none", "--file:channel-none-response.json"));
        loadFile("channel-set-response.json");
        loadFile("channel-remove-response.json");
        loadFile("channel-info-response.json");
        loadFile("channel-none-response.json");
    }

    @Override
    public void executeMore(MessagedString messagedString) {
        if (! messagedString.hasCommandArgs()) {
            messageInfo(messagedString);
            return;
        }

        String action = messagedString.getCommandArgs()[0].toLowerCase();
        switch (action) {
            case "set" -> {
                if (messagedString.getCommandArgs().length < 2) {
                    messageInfo(messagedString);
                    return;
                }

                EndPointType type;

                try {
                    type = EndPointType.valueOf(messagedString.getCommandArgs()[1].toUpperCase());
                } catch (Exception e) {
                    messageInfo(messagedString);
                    return;
                }

                if (type.equals(EndPointType.GLOBAL_NATIVE) && messagedString.getCommandArgs().length == 2) {
                    EndPoint discord = new EndPoint(EndPointType.DISCORD_TEXT,
                            messagedString.getChannel().getIdAsString(), DiscordModule.getConfig().getDefaultFormatFromMinecraft());
                    EndPoint other = new EndPoint(EndPointType.GLOBAL_NATIVE, "", DiscordModule.getConfig().getDefaultFormatFromDiscord());
                    Route toDiscord = new Route(other, discord);
                    Route toOther = new Route(discord, other);
                    DiscordHandler.loadRoute(toDiscord);
                    DiscordHandler.loadRoute(toOther);

                    messageSet(messagedString, other);
                    return;
                }

                if (messagedString.getCommandArgs().length < 3) {
                    messageInfo(messagedString);
                    return;
                }

                String otherFormat = type.equals(EndPointType.DISCORD_TEXT) ? DiscordModule.getConfig().getDefaultFormatFromDiscord()
                        : DiscordModule.getConfig().getDefaultFormatFromMinecraft();
                String discordFormat = DiscordModule.getConfig().getDefaultFormatFromDiscord();

                if (messagedString.getCommandArgs().length >= 4) {
                    otherFormat = ModuleUtils.argsToStringMinus(messagedString.getCommandArgs(), 0, 1, 2);
                }

                EndPoint discord = new EndPoint(EndPointType.DISCORD_TEXT,
                        messagedString.getChannel().getIdAsString(), otherFormat);
                EndPoint other = new EndPoint(type, messagedString.getCommandArgs()[2], discordFormat);
                Route toDiscord = new Route(other, discord);
                Route toOther = new Route(discord, other);
                DiscordHandler.loadRoute(toDiscord);
                DiscordHandler.loadRoute(toOther);

                messageSet(messagedString, other);
            }
            case "remove" -> {
                if (messagedString.getCommandArgs().length == 1) {
                    DiscordHandler.getAssociatedRoutes(EndPointType.DISCORD_TEXT, messagedString.getChannel().getIdAsString()).forEach(route -> {
                        messageRemove(messagedString, route.getOutput());
                        route.remove();
                    });
                    return;
                }

                if (messagedString.getCommandArgs().length < 3) {
                    messageInfo(messagedString);
                    return;
                }

                EndPointType type;

                try {
                    type = EndPointType.valueOf(messagedString.getCommandArgs()[1].toUpperCase());
                } catch (Exception e) {
                    messageInfo(messagedString);
                    return;
                }

                String identifier = messagedString.getCommandArgs()[2];

                ConcurrentSkipListSet<Route> routes = DiscordHandler.getBackAndForthRoute(type, identifier, messagedString.getChannel().getIdAsString());
                Optional<Route> thing = routes.stream().filter(route -> ! route.getInput().getType().equals(EndPointType.DISCORD_TEXT)).findFirst();

                if (thing.isEmpty()) {
                    messageNone(messagedString);
                    return;
                }

                EndPoint point = thing.get().getOutput();

                messageRemove(messagedString, point);

                routes.forEach(Route::remove);
            }
            default -> {
                messageInfo(messagedString);
            }
        }
    }

    public void messageSet(MessagedString messagedString, EndPoint endPoint) {
        message(messagedString, getReplyMessageSet(), endPoint);
    }

    public void messageRemove(MessagedString messagedString, EndPoint endPoint) {
        message(messagedString, getReplyMessageRemove(), endPoint);
    }

    public void messageInfo(MessagedString messagedString) {
        message(messagedString, getReplyMessageInfo());
    }

    public void messageNone(MessagedString messagedString) {
        message(messagedString, getReplyMessageNone());
    }

    public void message(MessagedString messagedString, String message) {
        if (isJsonFile(message)) {
            String json = getJsonFromFile(getJsonFile(message));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getIdAsString())
            ));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getId(), message
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getIdAsString())
            );
        }
    }

    public void message(MessagedString messagedString, String message, EndPoint endPoint) {
        if (isJsonFile(message)) {
            String json = getJsonFromFile(getJsonFile(message));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getId(),
                    ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(),
                            json
                                    .replace("%this_command_label%", messagedString.getBase())
                                    .replace("%this_channel_id%", messagedString.getChannel().getIdAsString())
                                    .replace("%this_type%", endPoint.getType().toString())
                                    .replace("%this_identifier%", endPoint.getIdentifier())
                                    .replace("%this_format%", endPoint.getToFormat())
                    )
            );
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getId(), message
                    .replace("%this_command_label%", messagedString.getBase())
                    .replace("%this_channel_id%", messagedString.getChannel().getIdAsString())
                    .replace("%this_type%", endPoint.getType().toString())
                    .replace("%this_identifier%", endPoint.getIdentifier())
                    .replace("%this_format%", endPoint.getToFormat())
            );
        }
    }
}
