package tv.quaint.discordmodule.depends;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.holders.ModuleDependencyHolder;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.ChannelMessageEvent;
import tv.quaint.events.processing.BaseProcessor;
import tv.quaint.savables.ChatterManager;

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

            DiscordModule.getInstance().logWarning("Found channel message with identifier '" + event.getChatChannel().identifier() + "'...");

            DiscordHandler.getLoadedChanneledFolders().forEach((string, folder) -> {
                folder.getLoadedRoutes().forEach((s, route) -> {
                    DiscordModule.getInstance().logWarning("Scanning route '" + route.getUuid() + "'");

                    if (!route.getInput().getType().equals(EndPointType.SPECIFIC_HANDLED)) return;
                    DiscordModule.getInstance().logWarning("PASS #1...");
                    if (!route.getInput().getIdentifier().equals(event.getChatChannel().identifier())) return;

                    DiscordModule.getInstance().logWarning("Bouncing message...");
                    route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
                });
            });
        }
    }

    public ConcurrentSkipListMap<String, StreamlineUser> getUsersInChannel(String identifier) {
        ConcurrentSkipListMap<String, StreamlineUser> r = new ConcurrentSkipListMap<>();
        if (! isPresent()) return r;

        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(identifier);
        if (channel == null) return r;

        ChatterManager.getChattersViewingChannel(channel).forEach(savableChatter -> {
            StreamlineUser user = savableChatter.asUser();
            if (user == null) {
                DiscordModule.getInstance().logWarning("Could not get StreamlineUser of SavableChatter with uuid '" + savableChatter.getUuid() + "'.");
                return;
            }

            r.put(user.getUuid(), user);
        });

        return r;
    }
}
