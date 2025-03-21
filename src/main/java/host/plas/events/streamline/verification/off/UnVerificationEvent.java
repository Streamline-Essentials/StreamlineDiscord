package host.plas.events.streamline.verification.off;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.events.streamline.verification.VerificationResultEvent;
import singularity.events.modules.ModuleEvent;

@Setter
@Getter
public class UnVerificationEvent extends ModuleEvent {
    private VerificationResultEvent.Result result;
    private boolean fromCommand;

    public UnVerificationEvent(VerificationResultEvent.Result result, boolean isFromCommand) {
        super(DiscordModule.getInstance());
        setResult(result);
        setFromCommand(isFromCommand);
    }
}
