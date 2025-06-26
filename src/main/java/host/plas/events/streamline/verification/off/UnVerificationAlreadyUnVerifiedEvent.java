package host.plas.events.streamline.verification.off;

import host.plas.events.streamline.verification.VerificationResultEvent;

public class UnVerificationAlreadyUnVerifiedEvent extends UnVerificationEvent {
    public UnVerificationAlreadyUnVerifiedEvent(boolean isFromCommand) {
        super(VerificationResultEvent.Result.ALREADY_UNVERIFIED, null, null, isFromCommand);
    }
}
