package tv.quaint.discordmodule.events;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.messages.events.ProxiedMessageEvent;
import net.streamline.api.messages.events.ProxyMessageInEvent;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.BotMessageConfig;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.messaging.DiscordProxiedMessage;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;
import tv.quaint.discordmodule.events.verification.VerificationAlreadyVerifiedEvent;
import tv.quaint.discordmodule.events.verification.VerificationFailureEvent;
import tv.quaint.discordmodule.events.verification.VerificationSuccessEvent;
import tv.quaint.discordmodule.server.events.DiscordEventMessageBuilder;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseEventPriority;
import tv.quaint.events.processing.BaseProcessor;

public class MainListener implements BaseEventListener {
    public MainListener() {
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @BaseProcessor
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

    @BaseProcessor
    public void onMessage(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (event.getMessage().getAuthor().isBot()) return;

        if (! event.getMessage().hasPrefix() || ! (DiscordModule.getConfig().getBotLayout().isSlashCommandsEnabled() && event.getMessage().hasSlashPrefix())) {
            if (DiscordHandler.hasVerification(event.getMessage().getTotalMessage())) {
                SingleSet<MessageCreateData, BotMessageConfig> data =
                        DiscordHandler.tryVerificationForUser(event.getMessage(), event.getMessage().getTotalMessage(), false);

                DiscordMessenger.message(event.getMessage().getChannel().getIdLong(), data.getKey(), data.getValue());
            }
            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                folder.getLoadedRoutes().forEach((s, route) -> {
                    if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(event.getMessage().getChannel().getId())) {
                        route.bounceMessage(new RoutedUser(event.getMessage().getAuthor().getIdLong()), event.getMessage().getTotalMessage());
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

    @BaseProcessor
    public void onCommand(DiscordCommandEvent event) {
        DiscordModule.getInstance().logDebug("Executing command '" + event.getCommand().getCommandIdentifier() + "'...!");
        event.getCommand().execute(event.getMessage());
    }

    @BaseProcessor(priority = BaseEventPriority.HIGHEST)
    public void onProxiedMessage(ProxiedMessageEvent event) {
        ProxiedMessage message = event.getMessage();

        if (message == null) return;
        if (message.getSubChannel().equals(DiscordProxiedMessage.getSelfSubChannel())) {
            DiscordModule.getInstance().logDebug("Got DiscordProxiedMessage...");
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

    @BaseProcessor
    public void onSimpleDiscordPMReceived(SimpleDiscordPMessageReceivedEvent event) {

    }

    @BaseProcessor
    public void onProxiedMessageReceived(ProxyMessageInEvent event) {
        if (DiscordHandler.isBackEnd()) return;

        ProxiedMessage message = event.getMessage();
        if (message == null) return;
        if (message.getSubChannel() == null) return;

        if (message.getSubChannel().equals(DiscordEventMessageBuilder.getSubChannel())) {
            DiscordEventMessageBuilder.handle(message);
        }
    }

    @BaseProcessor
    public void onVerificationSuccess(VerificationSuccessEvent event) {
        StreamlineUser user = ModuleUtils.getOrGetUser(event.getStreamlineUUID());
        if (user == null) {
            DiscordModule.getInstance().logWarning("Verified Discord ID '" + event.getMessage().getAuthor().getIdLong() + "', but the associated StreamlineUser is 'null'! Skipping...");
            return;
        }

        ModuleUtils.sendMessage(user, DiscordModule.getMessages().successMinecraft());
    }

    @BaseProcessor
    public void onVerificationFailureGeneric(VerificationFailureEvent event) {

    }

    @BaseProcessor
    public void onVerificationFailureAlreadyVerified(VerificationAlreadyVerifiedEvent event) {

    }
}
