package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;

public class VerificationSuccessEvent extends OnVerificationEvent {
    public VerificationSuccessEvent(MessagedString message, String uuid, String verification, boolean isFromCommand) {
        super(message, verification, uuid, Result.VERIFIED_SUCCESS, isFromCommand);
    }
}
