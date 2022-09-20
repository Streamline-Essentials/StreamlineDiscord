package tv.quaint.discordmodule.hooks.depends;

import net.streamline.api.SLAPI;
import tv.quaint.StreamlineGroups;
import net.streamline.api.holders.ModuleDependencyHolder;

public class GroupsDependency extends ModuleDependencyHolder<StreamlineGroups> {
    public GroupsDependency() {
        super("streamline-groups", "streamline-groups");
        if (super.isPresent()) {
            tryLoad(() -> {
                setApi(StreamlineGroups.getInstance());
                SLAPI.getInstance().getMessenger().logInfo("Hooked into Geyser! Enabling Geyser support!");
                return null;
            });
        } else {
            SLAPI.getInstance().getMessenger().logInfo("Did not detect a '" + getIdentifier() + "' plugin... Disabling support for '" + getIdentifier() + "'...");
        }
    }
}
