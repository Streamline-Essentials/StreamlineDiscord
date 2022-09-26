package tv.quaint.discordmodule.hooks.depends;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.holders.ModuleDependencyHolder;
import net.streamline.api.modules.ModuleManager;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.discord.saves.obj.channeling.RoutedUser;
import tv.quaint.events.ChannelMessageEvent;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.concurrent.ConcurrentSkipListMap;

public class MessagingDependency extends ModuleDependencyHolder<StreamlineMessaging> {
    @Getter @Setter
    private MessagingListener messagingListener;

    public MessagingDependency() {
        super("streamline-messaging", "streamline-messaging");
        if (super.isPresent()) {
            tryLoad(() -> {
                nativeLoad();
                setMessagingListener(new MessagingListener());
                ModuleUtils.listen(getMessagingListener(), DiscordModule.getInstance());
                return null;
            });
        } else {
            SLAPI.getInstance().getMessenger().logInfo("Did not detect a '" + getIdentifier() + "' plugin... Disabling support for '" + getIdentifier() + "'...");
        }
    }

    public static class MessagingListener implements StreamlineListener {
        @EventProcessor
        public void onChannelMessage(ChannelMessageEvent event) {
            DiscordHandler.getLoadedRoutes().forEach((s, route) -> {
                if (route.getInput().getType().equals(EndPointType.SPECIFIC_HANDLED))
                    if (route.getInput().getIdentifier().equals(event.getChatChannel().identifier()))
                        route.bounceMessage(new RoutedUser(event.getSender()), event.getMessage());
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
            if (user == null) return;

            r.put(user.getUuid(), user);
        });

        return r;
    }
}
