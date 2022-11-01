package tv.quaint.discordmodule.events;

import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.discordmodule.DiscordModule;

public class BotStoppedEvent extends ModuleEvent {
    public BotStoppedEvent() {
        super(DiscordModule.getInstance());
    }
}
