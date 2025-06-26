package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;

public class VerificationFailureEvent extends OnVerificationEvent {
    public VerificationFailureEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, verification, null, Result.VERIFIED_FAILURE, isFromCommand);
    }
}
