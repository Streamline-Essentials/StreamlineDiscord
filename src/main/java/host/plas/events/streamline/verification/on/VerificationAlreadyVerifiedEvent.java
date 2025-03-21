package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;
import host.plas.events.streamline.verification.VerificationResultEvent;

public class VerificationAlreadyVerifiedEvent extends VerificationResultEvent {
    public VerificationAlreadyVerifiedEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.ALREADY_VERIFIED, isFromCommand);
    }
}
