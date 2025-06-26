package host.plas.depends;

import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.events.processing.BaseProcessor;
import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.events.ChannelMessageEvent;
import host.plas.savables.ChatterManager;
import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.discord.saves.obj.channeling.RouteManager;
import host.plas.discord.saves.obj.channeling.RoutedUser;
import host.plas.discord.saves.obj.channeling.EndPointType;
import singularity.data.console.CosmicSender;
import singularity.holders.ModuleDependencyHolder;
import singularity.modules.ModuleUtils;

import java.util.concurrent.ConcurrentSkipListMap;

public class MessagingDependency extends ModuleDependencyHolder<StreamlineMessaging> {
    @Getter @Setter
    private MessagingListener messagingListener;

    public MessagingDependency() {
        super("streamline-messaging", "streamline-messaging");
        if (super.isPresent()) {
            tryLoad(() -> {
                nativeLoad();
                if (getMessagingListener() == null) {
                    setMessagingListener(new MessagingListener());
                    ModuleUtils.listen(getMessagingListener(), DiscordModule.getInstance());
                }
                return null;
            });
        } else {
            DiscordModule.getInstance().logInfo("Did not detect a '" + getIdentifier() + "' module... Disabling support for '" + getIdentifier() + "'...");
        }
    }

    public static class MessagingListener implements BaseEventListener {
        @BaseProcessor
        public void onChannelMessage(ChannelMessageEvent event) {
            if (! DiscordModule.getConfig().allowStreamlineChannelsToDiscord()) return;

            DiscordModule.getInstance().logDebug("Found channel message with identifier '" + event.getChatChannel().getIdentifier() + "'...");

            RouteManager.getLoadedRoutes().forEach(route -> {
                DiscordModule.getInstance().logDebug("Scanning route '" + route.getIdentifier() + "'");

                if (! route.getInput().getType().equals(EndPointType.SPECIFIC_HANDLED)) return;
                DiscordModule.getInstance().logWarning("PASS #1...");
                if (! route.getInput().getIdentifier().equals(event.getChatChannel().getIdentifier())) return;

                DiscordModule.getInstance().logWarning("Bouncing message...");
                route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
            });
        }
    }

    public ConcurrentSkipListMap<String, CosmicSender> getUsersInChannel(String identifier) {
        ConcurrentSkipListMap<String, CosmicSender> r = new ConcurrentSkipListMap<>();
        if (! isPresent()) return r;

        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(identifier);
        if (channel == null) return r;

        ChatterManager.getChattersViewingChannel(channel).forEach(savableChatter -> {
            CosmicSender user = savableChatter.asUser();
            if (user == null) {
                DiscordModule.getInstance().logWarning("Could not get StreamlineUser of SavableChatter with uuid '" + savableChatter.getUuid() + "'.");
                return;
            }

            r.put(user.getUuid(), user);
        });

        return r;
    }
}
