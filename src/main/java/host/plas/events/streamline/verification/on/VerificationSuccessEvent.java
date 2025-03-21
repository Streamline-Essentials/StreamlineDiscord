package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;
import host.plas.events.streamline.verification.VerificationResultEvent;

public class VerificationSuccessEvent extends VerificationResultEvent {
    public VerificationSuccessEvent(MessagedString message, String streamlineUUID, String verification, boolean isFromCommand) {
        super(message, streamlineUUID, verification, Result.VERIFIED_SUCCESS, isFromCommand);
    }
}
