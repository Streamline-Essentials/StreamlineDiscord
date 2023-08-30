package tv.quaint.events.verification.off;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.events.verification.VerificationResultEvent;

public class UnVerificationSuccessEvent extends UnVerificationEvent {
    @Getter @Setter
    private long discordId;
    @Getter @Setter
    private String minecraftId;

    public UnVerificationSuccessEvent(boolean isFromCommand, long discordId, String minecraftId) {
        super(VerificationResultEvent.Result.UNVERIFIED_SUCCESS, isFromCommand);
        setDiscordId(discordId);
        setMinecraftId(minecraftId);
    }
}
