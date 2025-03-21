package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;
import host.plas.events.streamline.verification.VerificationResultEvent;

public class VerificationFailureEvent extends VerificationResultEvent {
    public VerificationFailureEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.VERIFIED_FAILURE, isFromCommand);
    }
}
