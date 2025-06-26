package host.plas.events.streamline;

import host.plas.DiscordModule;
import singularity.events.modules.ModuleEvent;

public class CosmicDiscordEvent extends ModuleEvent {
    public CosmicDiscordEvent() {
        super(DiscordModule.getInstance());
    }
}
