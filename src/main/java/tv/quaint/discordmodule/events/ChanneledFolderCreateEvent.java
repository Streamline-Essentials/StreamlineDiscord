package tv.quaint.discordmodule.events;

import tv.quaint.discordmodule.discord.saves.obj.channeling.ChanneledFolder;

public class ChanneledFolderCreateEvent extends ChanneledFolderEvent {
    public ChanneledFolderCreateEvent(ChanneledFolder folder) {
        super(folder);
    }
}
