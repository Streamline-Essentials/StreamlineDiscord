package tv.quaint.events.verification.off;

import tv.quaint.events.verification.VerificationResultEvent;

public class UnVerificationAlreadyUnVerifiedEvent extends UnVerificationEvent {
    public UnVerificationAlreadyUnVerifiedEvent(boolean isFromCommand) {
        super(VerificationResultEvent.Result.ALREADY_UNVERIFIED, isFromCommand);
    }
}
