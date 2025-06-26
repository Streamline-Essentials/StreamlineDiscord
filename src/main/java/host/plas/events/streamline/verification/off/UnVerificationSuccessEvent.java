package host.plas.events.streamline.verification.off;

import host.plas.events.streamline.verification.VerificationResultEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnVerificationSuccessEvent extends UnVerificationEvent {
    public UnVerificationSuccessEvent(boolean isFromCommand, String uuid, long discordId) {
        super(VerificationResultEvent.Result.UNVERIFIED_SUCCESS, uuid, discordId, isFromCommand);
    }
}
