package tv.quaint.events.verification.off;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;
import tv.quaint.events.verification.VerificationResultEvent;

public class UnVerificationEvent extends ModuleEvent {
    @Getter @Setter
    private VerificationResultEvent.Result result;
    @Getter @Setter
    private boolean fromCommand;

    public UnVerificationEvent(VerificationResultEvent.Result result, boolean isFromCommand) {
        super(DiscordModule.getInstance());
        setResult(result);
        setFromCommand(isFromCommand);
    }
}
