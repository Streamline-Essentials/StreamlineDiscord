package tv.quaint.events.verification.on;

import tv.quaint.discord.MessagedString;
import tv.quaint.events.verification.VerificationResultEvent;

public class VerificationAlreadyVerifiedEvent extends VerificationResultEvent {
    public VerificationAlreadyVerifiedEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.ALREADY_VERIFIED, isFromCommand);
    }
}
