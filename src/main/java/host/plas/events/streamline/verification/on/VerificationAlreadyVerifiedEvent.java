package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;

public class VerificationAlreadyVerifiedEvent extends OnVerificationEvent {
    public VerificationAlreadyVerifiedEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, verification, null, Result.ALREADY_VERIFIED, isFromCommand);
    }
}
