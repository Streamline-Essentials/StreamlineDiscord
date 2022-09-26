package tv.quaint.discordmodule.events;

import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;
import tv.quaint.discordmodule.events.DiscordCommandEvent;
import tv.quaint.discordmodule.events.DiscordMessageEvent;
import tv.quaint.discordmodule.hooks.Hooks;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class MainListener implements StreamlineListener {
    public MainListener() {
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @EventProcessor
    public void onStreamlineMessage(StreamlineChatEvent event) {
        if (event.isCanceled()) return;

        ConcurrentSkipListSet<Route> routes = new ConcurrentSkipListSet<>(DiscordHandler.getLoadedRoutes().values());

        if (! Objects.requireNonNull(Hooks.BASE.get()).isEnabled()) {
            routes.forEach(route -> {
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
        }
    }

    @EventProcessor
    public void onMessage(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (event.getMessage().getSender().isBot()) return;

        if (DiscordHandler.hasVerification(event.getMessage().getTotalMessage())) {
            if (DiscordHandler.verifyUser(event.getMessage().getSender().getId(), event.getMessage().getTotalMessage()).join()) {
                StreamlineUser user = ModuleUtils.getOrGetUser(DiscordModule.getVerifiedUsers().discordIdToUUID(event.getMessage().getSender().getId()));
                if (user == null) {
                    DiscordModule.getInstance().logWarning("Verified Discord ID '" + event.getMessage().getSender().getId() + "', but the associated StreamlineUser is 'null'!");
                    return;
                }

                ModuleUtils.sendMessage(user, DiscordModule.getMessages().completedMinecraft());

                if (DiscordModule.isJsonFile(DiscordModule.getMessages().completedDiscord())) {
                    String fileName = DiscordModule.getJsonFile(DiscordModule.getMessages().completedDiscord());

                    CompletableFuture.runAsync(() -> {
                        DiscordModule.loadFile(fileName);
                    }).join();

                    DiscordMessenger.sendSimpleEmbed(event.getMessage().getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(user, DiscordModule.getJsonFromFile(fileName)));
                } else {
                    DiscordMessenger.sendMessage(event.getMessage().getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(user, DiscordModule.getMessages().completedDiscord()));
                }
                return;
            }
        }

        if (! event.getMessage().hasPrefix()) {
            DiscordHandler.getLoadedRoutes().forEach((s, route) -> {
                if (route.getInput().getType().equals(EndPointType.DISCORD_TEXT)) route.bounceMessage(
                        new RoutedUser(event.getMessage().getSender().getId()), event.getMessage().getTotalMessage());
            });
        }

        DiscordCommand command = DiscordHandler.getCommandByAlias(event.getMessage().getBase());
        if (command == null) {
            DiscordModule.getInstance().logWarning("Could not get DiscordCommand with alias of '" + event.getMessage().getBase() + "'.");
            return;
        }

        ModuleUtils.fireEvent(new DiscordCommandEvent(event.getMessage(), command));
    }

    @EventProcessor
    public void onCommand(DiscordCommandEvent event) {
        DiscordModule.getInstance().logInfo("Executing command '" + event.getCommand().getCommandIdentifier() + "'...!");
        event.getCommand().execute(event.getMessage());
    }
}
