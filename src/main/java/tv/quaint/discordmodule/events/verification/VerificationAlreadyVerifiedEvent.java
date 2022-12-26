package tv.quaint.discordmodule.events.verification;

import tv.quaint.discordmodule.discord.MessagedString;

public class VerificationAlreadyVerifiedEvent extends VerificationResultEvent {
    public VerificationAlreadyVerifiedEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.ALREADY_VERIFIED, isFromCommand);
    }
}
