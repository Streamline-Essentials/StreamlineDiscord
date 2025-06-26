package host.plas.events.streamline.verification;

import host.plas.events.streamline.CosmicDiscordEvent;

public class VerificationEvent extends CosmicDiscordEvent {
    private boolean fromCommand;

    public VerificationEvent(boolean isFromCommand) {
        super();
        this.fromCommand = isFromCommand;
    }
}
