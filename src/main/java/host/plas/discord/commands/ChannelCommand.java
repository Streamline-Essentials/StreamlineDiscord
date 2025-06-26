package host.plas.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import host.plas.DiscordModule;
import host.plas.discord.DiscordCommand;
import host.plas.discord.MessagedString;
import host.plas.discord.messaging.BotMessageConfig;
import host.plas.discord.messaging.DiscordMessenger;
import singularity.modules.ModuleUtils;
import singularity.objects.SingleSet;
import singularity.utils.UserUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
public class ChannelCommand extends DiscordCommand {
    private String replyMessageSet;
    private String replyMessageRemove;
    private String replyMessageInfo;
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
    public CommandCreateAction setupOptionData(CommandCreateAction action) {
        return action.addOption(OptionType.STRING, "action", "The action to perform.", true)
                .addOption(OptionType.STRING, "type", "The type of channel to perform the action on.", false)
                .addOption(OptionType.STRING, "identifier", "The identifier of the channel to perform the action on.", false);
    }

    @Override
    public SingleSet<MessageCreateData, BotMessageConfig> executeMore(MessagedString messagedString) {
        if (! messagedString.hasCommandArgs()) {
            return messageInfo(messagedString);
        }

        String action = messagedString.getCommandArgs()[0].toLowerCase();
        switch (action) {
            case "set":
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
                    EndPoint discord = new EndPoint();

                    discord.setType(EndPointType.DISCORD_TEXT);
                    discord.setIdentifier(messagedString.getChannel().getId());
                    discord.setToFormat(DiscordModule.getConfig().getDefaultFormatFromMinecraft());

                    EndPoint other = new EndPoint();

                    other.setType(EndPointType.GLOBAL_NATIVE);
                    other.setIdentifier("");
                    other.setToFormat(DiscordModule.getConfig().getDefaultFormatFromDiscord());

                    Route toDiscord = new Route();

                    toDiscord.setInput(other);
                    toDiscord.setOutput(discord);

                    Route toOther = new Route();

                    toOther.setInput(discord);
                    toOther.setOutput(other);

                    RouteManager.registerRoute(toDiscord);
                    RouteManager.registerRoute(toOther);

                    toDiscord.save();
                    toOther.save();

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

                EndPoint discord = new EndPoint();

                discord.setType(EndPointType.DISCORD_TEXT);
                discord.setIdentifier(messagedString.getChannel().getId());
                discord.setToFormat(otherFormat);

                EndPoint other = new EndPoint();

                other.setType(EndPointType.GLOBAL_NATIVE);
                other.setIdentifier(messagedString.getCommandArgs()[2]);
                other.setToFormat(discordFormat);

                Route toDiscord = new Route();

                toDiscord.setInput(other);
                toDiscord.setOutput(discord);

                Route toOther = new Route();

                toOther.setInput(discord);
                toOther.setOutput(other);

                RouteManager.registerRoute(toDiscord);
                RouteManager.registerRoute(toOther);

                toDiscord.save();
                toOther.save();

                return messageSet(messagedString, other);
            case "remove":
                if (messagedString.getCommandArgs().length == 1) {
                    AtomicReference<SingleSet<MessageCreateData, BotMessageConfig>> data = new AtomicReference<>(DiscordMessenger.simpleMessage("No channel found to remove!"));

                    RouteManager.getLoadedRoutes().forEach(route -> {
                        if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(messagedString.getChannel().getId())) {
                            data.set(messageRemove(messagedString, route.getOutput()));
                            route.drop();
                        }
                    });

                    return data.get();
                }

                if (messagedString.getCommandArgs().length < 3) {
                    return messageInfo(messagedString);
                }

                EndPointType typeRemove;

                try {
                    typeRemove = EndPointType.valueOf(messagedString.getCommandArgs()[1].toUpperCase());
                } catch (Exception e) {
                    return messageInfo(messagedString);
                }

                String identifier = messagedString.getCommandArgs()[2];

                ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>();
                routes.addAll(RouteManager.getBackAndForthRoute(typeRemove, identifier, EndPointType.DISCORD_TEXT, messagedString.getChannel().getId()));

                Optional<Route> thing = routes.stream().filter(route -> ! route.getInput().getType().equals(EndPointType.DISCORD_TEXT)).findFirst();

                if (thing.isEmpty()) {
                    return messageNone(messagedString);
                }

                EndPoint point = thing.get().getOutput();

                SingleSet<MessageCreateData, BotMessageConfig> data = messageRemove(messagedString, point);

                routes.forEach(Route::drop);

                return data;
            default:
                return messageInfo(messagedString);
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
            return DiscordMessenger.simpleEmbed(ModuleUtils.replacePlaceholders(UserUtils.getConsole(), json
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
                    ModuleUtils.replacePlaceholders(UserUtils.getConsole(),
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
