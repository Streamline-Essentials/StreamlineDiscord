package tv.quaint.events.verification.off;

import tv.quaint.events.verification.VerificationResultEvent;

public class UnVerificationFailureEvent extends UnVerificationEvent {
    public UnVerificationFailureEvent(boolean isFromCommand) {
        super(VerificationResultEvent.Result.UNVERIFIED_FAILURE, isFromCommand);
    }
}
