package host.plas.events.streamline.bot;

import host.plas.DiscordModule;
import singularity.events.modules.ModuleEvent;

public class BotStoppedEvent extends ModuleEvent {
    public BotStoppedEvent() {
        super(DiscordModule.getInstance());
    }
}
