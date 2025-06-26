package host.plas.events.streamline.verification.off;

import host.plas.events.streamline.verification.VerificationResultEvent;

public class UnVerificationFailureEvent extends UnVerificationEvent {
    public UnVerificationFailureEvent(boolean isFromCommand) {
        super(VerificationResultEvent.Result.UNVERIFIED_FAILURE, null, null, isFromCommand);
    }
}
