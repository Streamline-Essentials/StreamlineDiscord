package tv.quaint.discordmodule.hooks.depends;

import net.streamline.api.SLAPI;
import net.streamline.api.modules.ModuleManager;
import tv.quaint.StreamlineGroups;
import net.streamline.api.holders.ModuleDependencyHolder;

public class GroupsDependency extends ModuleDependencyHolder<StreamlineGroups> {
    public GroupsDependency() {
        super("streamline-groups", "streamline-groups");
        if (super.isPresent()) {
            tryLoad(() -> {
                nativeLoad();
                return null;
            });
        } else {
            SLAPI.getInstance().getMessenger().logInfo("Did not detect a '" + getIdentifier() + "' plugin... Disabling support for '" + getIdentifier() + "'...");
        }
    }
}
