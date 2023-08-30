package tv.quaint.events;

import tv.quaint.discord.saves.obj.channeling.ChanneledFolder;

public class ChanneledFolderCreateEvent extends ChanneledFolderEvent {
    public ChanneledFolderCreateEvent(ChanneledFolder folder) {
        super(folder);
    }
}
