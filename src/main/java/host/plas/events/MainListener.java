package host.plas.events;

import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.events.processing.BaseEventPriority;
import gg.drak.thebase.events.processing.BaseProcessor;
import host.plas.discord.data.channeling.EndPointType;
import host.plas.discord.data.channeling.RouteManager;
import host.plas.discord.data.channeling.RoutedUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import host.plas.DiscordModule;
import host.plas.discord.DiscordCommand;
import host.plas.discord.DiscordHandler;
import host.plas.discord.messaging.BotMessageConfig;
import host.plas.discord.messaging.DiscordMessenger;
import host.plas.discord.messaging.DiscordProxiedMessage;
import singularity.data.console.CosmicSender;
import singularity.events.server.CosmicChatEvent;
import singularity.messages.events.ProxiedMessageEvent;
import singularity.messages.events.ProxyMessageInEvent;
import singularity.messages.proxied.ProxiedMessage;
import singularity.modules.ModuleUtils;
import singularity.objects.SingleSet;
import singularity.utils.UserUtils;
import host.plas.events.streamline.bot.command.DiscordCommandEvent;
import host.plas.events.streamline.bot.posting.DiscordMessageEvent;
import host.plas.events.streamline.proxy.SimpleDiscordPMessageReceivedEvent;
import host.plas.events.streamline.verification.off.UnVerificationSuccessEvent;
import host.plas.events.streamline.verification.on.VerificationSuccessEvent;

public class MainListener implements BaseEventListener {
    public MainListener() {
        ModuleUtils.listen(this, DiscordModule.getInstance());
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @BaseProcessor
    public void onStreamlineMessage(CosmicChatEvent event) {
        if (event.isCanceled()) return;
        if (ModuleUtils.isCommand(event.getMessage())) return;
        RouteManager.getLoadedRoutes().forEach(route -> {
            switch (route.getInput().getType()) {
                case GLOBAL_NATIVE:
                    route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
                    break;
                case SPECIFIC_NATIVE:
                    if (event.getSender().getServerName().equals(route.getInput().getIdentifier())) {
                        route.bounceMessage(
                                new RoutedUser(event.getSender()), event.getMessage());
                    }
                    break;
                case PERMISSION:
                    if (ModuleUtils.hasPermission(event.getSender(), route.getInput().getIdentifier())) {
                        route.bounceMessage(
                                new RoutedUser(event.getSender()), event.getMessage());
                    }
                    break;
            }
        });
    }

    @BaseProcessor
    public void onMessage(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (event.getMessage().getAuthor().isBot()) return;

        if (! event.getMessage().hasPrefix() || ! (DiscordModule.getConfig().getBotLayout().isSlashCommandsEnabled() && event.getMessage().hasSlashPrefix())) {
            if (! DiscordModule.getConfig().verificationOnlyCommand()) {
                if (DiscordHandler.hasVerification(event.getMessage().getTotalMessage())) {
                    SingleSet<MessageCreateData, BotMessageConfig> data =
                            DiscordHandler.tryVerificationForUser(event.getMessage(), event.getMessage().getTotalMessage(), false);

                    DiscordMessenger.message(event.getMessage().getChannel().getIdLong(), data.getKey(), data.getValue());
                }
            }
            RouteManager.getLoadedRoutes().forEach(route -> {
                if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT) && route.getInput().getIdentifier().equals(event.getMessage().getChannel().getId())) {
                    route.bounceMessage(new RoutedUser(event.getMessage().getAuthor().getIdLong()), event.getMessage().getTotalMessage());
                }
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

            EndPointType finalType = type;
            RouteManager.getLoadedRoutes().forEach(route -> {
                if (route.getInput().getType().equals(finalType) && route.getInput().getIdentifier().equals(ev.simplyGetInputIdentifier())) {
                    route.bounceMessage(new RoutedUser(UserUtils.getConsole()), ev.simplyGetMessage(),
                            ev.simplyGetMessage().startsWith("{") && ev.simplyGetMessage().endsWith("}"));
                }
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

        // TODO: Check if this is needed.
//        if (message.getSubChannel().equals(DiscordEventMessageBuilder.getSubChannel())) {
//            DiscordEventMessageBuilder.handle(message);
//        }
    }

    @BaseProcessor
    public void onVerificationSuccess(VerificationSuccessEvent event) {
        CosmicSender user = event.uuidAsUser().orElse(null);
        if (user == null) {
            DiscordModule.getInstance().logWarning("Verified Discord ID '" + event.getMessage().getAuthor().getIdLong() + "', but the associated StreamlineUser is 'null'! Skipping...");
            return;
        }

        ModuleUtils.sendMessage(user, DiscordModule.getMessages().verifySuccessMinecraft());

        try {
            if (DiscordModule.getConfig().verificationEventVerifiedDiscordEnabled()) {
                Guild guild = DiscordHandler.getServerById(DiscordModule.getConfig().getBotLayout().getMainGuildId());
                for (long id : DiscordModule.getConfig().verificationEventVerifiedDiscordRoles()) {
                    Role r = guild.getRoleById(id);
                    if (r != null) {
                        try {
                            guild.addRoleToMember(event.getMessage().getAuthor(), r).queue();
                        } catch (Exception e) {
                            DiscordModule.getInstance().logSevere("Could not add role '" + r.getName() + "' to user '" + event.getMessage().getAuthor().getAsTag() + "'! Are they an admin? (Cannot add roles to admins and server owners...)");
                        }
                    }
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (DiscordModule.getConfig().verificationEventVerifiedMinecraftEnabled()) {
                for (String command : DiscordModule.getConfig().verificationEventVerifiedCommandsList()) {
                    ModuleUtils.queueRunAs(user, command);
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BaseProcessor
    public void onUnVerificationSuccess(UnVerificationSuccessEvent event) {
        CosmicSender user = event.getSender().orElse(null);
        if (user == null) {
            DiscordModule.getInstance().logWarning("UnVerified Discord ID '" + event.getDiscordId() + "', but the associated StreamlineUser is 'null'! Skipping...");
            return;
        }

        ModuleUtils.sendMessage(user, DiscordModule.getMessages().unVerifySuccessMinecraft());

        User u = DiscordHandler.getUser(event.getDiscordId());
        if (u == null) {
            DiscordModule.getInstance().logWarning("UnVerified Discord ID '" + event.getDiscordId() + "', but the associated Discord User is 'null'! Skipping...");
            return;
        }

        try {
            if (DiscordModule.getConfig().verificationEventUnVerifiedDiscordEnabled()) {
                Guild guild = DiscordHandler.getServerById(DiscordModule.getConfig().getBotLayout().getMainGuildId());
                for (long id : DiscordModule.getConfig().verificationEventUnVerifiedDiscordRoles()) {
                    Role r = guild.getRoleById(id);
                    if (r != null) {
                        try {
                            guild.removeRoleFromMember(u, r).queue();
                        } catch (Exception e) {
                            DiscordModule.getInstance().logSevere("Could not remove role '" + r.getName() + "' to user '" + u.getAsTag() + "'! Are they an admin? (Cannot add roles to admins and server owners...)");
                        }
                    }
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (DiscordModule.getConfig().verificationEventUnVerifiedMinecraftEnabled()) {
                for (String command : DiscordModule.getConfig().verificationEventUnVerifiedCommandsList()) {
                    ModuleUtils.queueRunAs(user, command);
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
