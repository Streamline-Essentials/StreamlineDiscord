package tv.quaint.discordmodule.hooks.depends;

import net.streamline.api.SLAPI;
import net.streamline.api.holders.ModuleDependencyHolder;
import tv.quaint.StreamlineMessaging;

public class MessagingDependency extends ModuleDependencyHolder<StreamlineMessaging> {
    public MessagingDependency() {
        super("streamline-messaging", "streamline-messaging");
        if (super.isPresent()) {
            tryLoad(() -> {
                setApi(StreamlineMessaging.getInstance());
                SLAPI.getInstance().getMessenger().logInfo("Hooked into Geyser! Enabling Geyser support!");
                return null;
            });
        } else {
            SLAPI.getInstance().getMessenger().logInfo("Did not detect a '" + getIdentifier() + "' plugin... Disabling support for '" + getIdentifier() + "'...");
        }
    }
}
