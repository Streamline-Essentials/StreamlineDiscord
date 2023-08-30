package tv.quaint.events.verification.on;

import tv.quaint.discord.MessagedString;
import tv.quaint.events.verification.VerificationResultEvent;

public class VerificationSuccessEvent extends VerificationResultEvent {
    public VerificationSuccessEvent(MessagedString message, String streamlineUUID, String verification, boolean isFromCommand) {
        super(message, streamlineUUID, verification, Result.VERIFIED_SUCCESS, isFromCommand);
    }
}
