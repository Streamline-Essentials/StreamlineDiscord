package tv.quaint.discordmodule.events.verification;

import tv.quaint.discordmodule.discord.MessagedString;

public class VerificationFailureEvent extends VerificationResultEvent {
    public VerificationFailureEvent(MessagedString message, String verification, boolean isFromCommand) {
        super(message, null, verification, Result.FAILURE, isFromCommand);
    }
}
