package tv.quaint.discordmodule.events.verification;

import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.MessagedString;

public class VerificationSuccessEvent extends VerificationResultEvent {
    public VerificationSuccessEvent(MessagedString message, String streamlineUUID, String verification, boolean isFromCommand) {
        super(message, streamlineUUID, verification, Result.SUCCESS, isFromCommand);
    }
}
