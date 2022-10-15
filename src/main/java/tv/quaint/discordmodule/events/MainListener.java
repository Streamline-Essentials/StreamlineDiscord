package tv.quaint.discordmodule.events;

import net.streamline.api.events.EventPriority;
import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.messages.events.ProxiedMessageEvent;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.MessageUtils;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.messaging.DiscordProxiedMessage;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;

public class MainListener implements StreamlineListener {
    public MainListener() {
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @EventProcessor
    public void onStreamlineMessage(StreamlineChatEvent event) {
        if (event.isCanceled()) return;
        if (ModuleUtils.isCommand(event.getMessage())) return;
        DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
            folder.getLoadedRoutes().forEach((s, route) -> {
                switch (route.getInput().getType()) {
                    case GLOBAL_NATIVE -> {
                        route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
                    }
                    case SPECIFIC_NATIVE -> {
                        if (event.getSender().getLatestServer().equals(route.getInput().getIdentifier())) {
                            route.bounceMessage(
                                    new RoutedUser(event.getSender()), event.getMessage());
                        }
                    }
                    case PERMISSION -> {
                        if (ModuleUtils.hasPermission(event.getSender(), route.getInput().getIdentifier())) {
                            route.bounceMessage(
                                    new RoutedUser(event.getSender()), event.getMessage());
                        }
                    }
                }
            });
        });
    }

    @EventProcessor
    public void onMessage(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (event.getMessage().getSender().isBot()) return;

        if (DiscordHandler.hasVerification(event.getMessage().getTotalMessage())) {
            if (DiscordHandler.verifyUser(event.getMessage().getSender().getId(), event.getMessage().getTotalMessage())) {
                StreamlineUser user = ModuleUtils.getOrGetUser(DiscordModule.getVerifiedUsers().discordIdToUUID(event.getMessage().getSender().getId()));
                if (user == null) {
                    DiscordModule.getInstance().logWarning("Verified Discord ID '" + event.getMessage().getSender().getId() + "', but the associated StreamlineUser is 'null'! Skipping...");
                    return;
                }

                ModuleUtils.sendMessage(user, DiscordModule.getMessages().completedMinecraft());

                if (DiscordModule.isJsonFile(DiscordModule.getMessages().completedDiscord())) {
                    String fileName = DiscordModule.getJsonFile(DiscordModule.getMessages().completedDiscord());

                    DiscordModule.loadFile(fileName);

                    DiscordMessenger.sendSimpleEmbed(event.getMessage().getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(user, DiscordModule.getJsonFromFile(fileName)));
                } else {
                    DiscordMessenger.sendMessage(event.getMessage().getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(user, DiscordModule.getMessages().completedDiscord()));
                }
                return;
            }
        }

        if (! event.getMessage().hasPrefix()) {
            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                folder.getLoadedRoutes().forEach((s, route) -> {
                    if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(event.getMessage().getChannel().getIdAsString())) {
                        route.bounceMessage(new RoutedUser(event.getMessage().getSender().getId()), event.getMessage().getTotalMessage());
                    }
                });
            });
        } else {
            DiscordCommand command = DiscordHandler.getCommandByAlias(event.getMessage().getBase());
            if (command == null) {
                DiscordModule.getInstance().logDebug("Could not get DiscordCommand with alias of '" + event.getMessage().getBase() + "'.");
                return;
            }

            ModuleUtils.fireEvent(new DiscordCommandEvent(event.getMessage(), command));
        }
    }

    @EventProcessor
    public void onCommand(DiscordCommandEvent event) {
        DiscordModule.getInstance().logDebug("Executing command '" + event.getCommand().getCommandIdentifier() + "'...!");
        event.getCommand().execute(event.getMessage());
    }

    @EventProcessor(priority = EventPriority.LOWEST)
    public void onProxiedMessage(ProxiedMessageEvent event) {
        ProxiedMessage message = event.getMessage();

        if (message == null) return;
        if (message.getSubChannel().equals(DiscordProxiedMessage.getSelfSubChannel())) {
            DiscordProxiedMessage discordProxiedMessage = DiscordProxiedMessage.translate(message);
            SimpleDiscordPMessageReceivedEvent ev = new SimpleDiscordPMessageReceivedEvent(discordProxiedMessage).fire();
            if (ev.isCancelled()) return;
            EndPointType type = null;
            try {
                type = EndPointType.valueOf(ev.simplyGetInputType());
            } catch (Exception e) {
                DiscordModule.getInstance().logSevere("Could not parse EndPointType from a received DiscordProxyMessage...");
                return;
            }

            DiscordHandler.pollAllChanneledFolders();
            EndPointType finalType = type;
            DiscordHandler.getLoadedChanneledFolders().forEach((s, folder) -> {
                folder.getLoadedRoutes().forEach((s1, route) -> {
                    if (route.getInput().getType().equals(finalType) && route.getInput().getIdentifier().equals(ev.simplyGetInputIdentifier())) {
                        route.bounceMessage(new RoutedUser(UserUtils.getConsole()), ev.simplyGetMessage(),
                                ev.simplyGetMessage().startsWith("{") && ev.simplyGetMessage().endsWith("}"));
                    }
                });
            });
        }
    }

    @EventProcessor
    public void onSimpleDiscordPMReceived(SimpleDiscordPMessageReceivedEvent event) {

    }
}
