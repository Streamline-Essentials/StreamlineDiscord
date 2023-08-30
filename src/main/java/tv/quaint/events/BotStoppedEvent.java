package tv.quaint.events;

import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;

public class BotStoppedEvent extends ModuleEvent {
    public BotStoppedEvent() {
        super(DiscordModule.getInstance());
    }
}
