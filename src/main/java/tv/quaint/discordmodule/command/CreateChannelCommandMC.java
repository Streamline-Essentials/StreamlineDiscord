package tv.quaint.discordmodule.command;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.MessageUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.*;
import tv.quaint.discordmodule.server.events.spigot.SpigotEventManager;
import tv.quaint.discordmodule.server.events.streamline.LoginDSLEvent;
import tv.quaint.discordmodule.server.events.streamline.LogoutDSLEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class CreateChannelCommandMC extends ModuleCommand {
    String messageReplySet;
    String messageReplyRemove;
    String messageReplyInfo;
    String messageReplyNone;

    public CreateChannelCommandMC() {
        super(DiscordModule.getInstance(),
                "creatediscordchannel",
                "streamline.command.create-channel.default",
                "cdiscordchannel", "cdiscordc", "cdiscord", "cdc", "create-channel", "createchannel", "createc"
        );

        messageReplySet = getCommandResource().getOrSetDefault("messages.reply.set",
                "&eLinked &b%this_channel_id% &eto &b%this_type% &ewith an identifier of &b%this_identifier% &eand a format of " +
                        "&b%this_format%%newline%&7More information and help at &bhttps://github.com/Streamline-Essentials/StreamlineWiki/wiki/Discord-Setup#setting-and-removing-channel-routes");
        messageReplyRemove = getCommandResource().getOrSetDefault("messages.reply.remove",
                "&eRemoved &b%this_channel_id% &efrom &b%this_type% &ewith an identifier of &b%this_identifier% &eand a format of " +
                        "&b%this_format%%newline%&7More information and help at &bhttps://github.com/Streamline-Essentials/StreamlineWiki/wiki/Discord-Setup#setting-and-removing-channel-routes");
        messageReplyInfo = getCommandResource().getOrSetDefault("messages.reply.info",
                "&eThis channel is linked to &b%this_type% &ewith an identifier of &b%this_identifier% &eand a format of " +
                        "&b%this_format%%newline%&7More information and help at &bhttps://github.com/Streamline-Essentials/StreamlineWiki/wiki/Discord-Setup#setting-and-removing-channel-routes");
        messageReplyNone = getCommandResource().getOrSetDefault("messages.reply.none",
                "&eWe could not find any route like that...%newline%&7More information and help at &bhttps://github.com/Streamline-Essentials/StreamlineWiki/wiki/Discord-Setup#setting-and-removing-channel-routes");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        String action = strings[0].toLowerCase();
        switch (action) {
            case "set" -> {
                if (strings.length < 6) {
                    messageInfo(streamlineUser);
                    return;
                }

                EndPointType typeIn;

                try {
                    typeIn = EndPointType.valueOf(strings[2].toUpperCase());
                } catch (Exception e) {
                    messageInfo(streamlineUser);
                    return;
                }

                EndPointType typeOut;

                try {
                    typeOut = EndPointType.valueOf(strings[4].toUpperCase());
                } catch (Exception e) {
                    messageInfo(streamlineUser);
                    return;
                }

                String outputFormat = typeIn.equals(EndPointType.DISCORD_TEXT) ? DiscordModule.getConfig().getDefaultFormatFromDiscord()
                        : DiscordModule.getConfig().getDefaultFormatFromMinecraft();
                String inputFormat = typeOut.equals(EndPointType.DISCORD_TEXT) ? DiscordModule.getConfig().getDefaultFormatFromDiscord()
                        : DiscordModule.getConfig().getDefaultFormatFromMinecraft();

                EndPoint input = new EndPoint(EndPointType.DISCORD_TEXT,
                        strings[1], outputFormat);
                EndPoint output = new EndPoint(typeIn, strings[3], inputFormat);
                ChanneledFolder folder = new ChanneledFolder(input.getType() + "-" + input.getIdentifier());
                Route toInput = new Route(output, input, folder);
                Route toOutput = new Route(input, output, folder);

                if (DiscordModule.getConfig().serverEventAllEventsOnDiscordRoute()) {
                    if (DiscordHandler.containsServerEvent("login")) {
                        ServerEventRoute<LoginDSLEvent> r = new ServerEventRoute<>(output, folder, DiscordHandler.getServerEvent(LoginDSLEvent.class));
                        folder.loadEventRoute(r);
                    }
                    if (DiscordHandler.containsServerEvent("logout")) {
                        ServerEventRoute<LogoutDSLEvent> r = new ServerEventRoute<>(output, folder, DiscordHandler.getServerEvent(LogoutDSLEvent.class));
                        folder.loadEventRoute(r);
                    }
                    if (DiscordHandler.isBackEnd()) {
                        SpigotEventManager.addAdvancementEvent(output, folder);
                        SpigotEventManager.addDeathEvent(output, folder);
                    }
                }

                folder.loadRoute(toInput);
                folder.loadRoute(toOutput);
                DiscordHandler.loadChanneledFolder(folder);

                DiscordHandler.pollAllChanneledFolders();

                messageSet(streamlineUser, toOutput);
            }
            case "remove" -> {
                if (strings.length == 1) {
                    DiscordHandler.getAllCurrentRoutes(streamlineUser).forEach((route) -> {
                        route.remove();

                        DiscordHandler.pollAllChanneledFolders();
                    });
                    return;
                }

                if (strings.length < 6) {
                    messageInfo(streamlineUser);
                    return;
                }

                EndPointType typeIn;

                try {
                    typeIn = EndPointType.valueOf(strings[2].toUpperCase());
                } catch (Exception e) {
                    messageInfo(streamlineUser);
                    return;
                }

                EndPointType typeOut;

                ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>();
                DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                    routes.addAll(folder.getBackAndForthRoute(typeIn, strings[1], strings[3]));
                });

                Optional<Route> thing = routes.stream().findFirst();

                if (thing.isEmpty()) {
                    ModuleUtils.sendMessage(streamlineUser, ModuleUtils.replaceAllPlayerBungee(streamlineUser, messageReplyNone));
                    return;
                }

                EndPoint point = thing.get().getOutput();

                routes.forEach(route -> {
                    route.remove();
                    messageRemove(streamlineUser, thing.get());
                });

                DiscordHandler.pollAllChanneledFolders();
            }
            default -> {
                messageInfo(streamlineUser);
            }
        }
    }

    public void messageInfo(StreamlineUser sender) {
        if (! (sender instanceof StreamlinePlayer player)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        DiscordHandler.getAllCurrentRoutes(player).forEach(route -> {
            ModuleUtils.sendMessage(sender, getWithOther(messageReplyInfo, player, route));
        });
    }

    public void messageSet(StreamlineUser sender, Route route) {
        if (! (sender instanceof StreamlinePlayer player)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        ModuleUtils.sendMessage(sender, getWithOther(messageReplySet, player, route));
    }

    public void messageRemove(StreamlineUser sender, Route route) {
        if (! (sender instanceof StreamlinePlayer player)) {
            ModuleUtils.sendMessage(sender, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        ModuleUtils.sendMessage(sender, getWithOther(messageReplyRemove, player, route));
    }

    public String getWithOther(String message, StreamlineUser user, Route route) {
        return getWithOther(user, message
                .replace("%this_input_type%", route.getInput().getType().toString())
                .replace("%this_input_channel_id%", route.getInput().getType().toString())
                .replace("%this_input_identifier%", route.getInput().getIdentifier())
                .replace("%this_output_type%", route.getOutput().getType().toString())
                .replace("%this_output_channel_id%", route.getOutput().getType().toString())
                .replace("%this_output_identifier%", route.getOutput().getIdentifier()), user)
                .replace("%this_input_format%", route.getInput().getToFormat())
                .replace("%this_output_format%", route.getOutput().getToFormat());
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser streamlineUser, String[] strings) {
        if (strings.length <= 1) {
            return new ConcurrentSkipListSet<>(Arrays.asList("info", "set", "remove"));
        }
        if (strings.length == 2) {
            return new ConcurrentSkipListSet<>(List.of("identifier"));
        }
        if (strings.length == 3) {
            return DiscordHandler.allEndPointTypesAsStrings();
        }
        if (strings.length == 4) {
            return new ConcurrentSkipListSet<>(List.of("identifier"));
        }
        if (strings.length == 5) {
            return DiscordHandler.allEndPointTypesAsStrings();
        }

        return new ConcurrentSkipListSet<>();
    }
}
