package host.plas.events.streamline.verification.off;

import lombok.Getter;
import lombok.Setter;
import host.plas.events.streamline.verification.VerificationResultEvent;

@Setter
@Getter
public class UnVerificationEvent extends VerificationResultEvent {
    private VerificationResultEvent.Result result;
    private boolean fromCommand;

    public UnVerificationEvent(VerificationResultEvent.Result result, String uuid, Long discordId, boolean isFromCommand) {
        super(uuid, discordId, result, isFromCommand);
        setResult(result);
        setFromCommand(isFromCommand);
    }
}
