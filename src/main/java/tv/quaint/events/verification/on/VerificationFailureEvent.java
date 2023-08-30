package tv.quaint.events.verification.on;

import tv.quaint.discord.MessagedString;
import tv.quaint.events.verification.VerificationResultEvent;

public class VerificationFailureEvent extends VerificationResultEvent {
    public VerificationFailureEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.VERIFIED_FAILURE, isFromCommand);
    }
}
