package tv.quaint.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;
import tv.quaint.discord.saves.obj.channeling.ChanneledFolder;

public class ChanneledFolderEvent extends ModuleEvent {
    @Getter @Setter
    private ChanneledFolder folder;

    public ChanneledFolderEvent(ChanneledFolder folder) {
        super(DiscordModule.getInstance());
        setFolder(folder);
    }
}
