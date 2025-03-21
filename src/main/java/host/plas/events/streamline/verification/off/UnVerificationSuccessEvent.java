package host.plas.events.streamline.verification.off;

import lombok.Getter;
import lombok.Setter;
import host.plas.events.streamline.verification.VerificationResultEvent;

@Setter
@Getter
public class UnVerificationSuccessEvent extends UnVerificationEvent {
    private long discordId;
    private String minecraftId;

    public UnVerificationSuccessEvent(boolean isFromCommand, long discordId, String minecraftId) {
        super(VerificationResultEvent.Result.UNVERIFIED_SUCCESS, isFromCommand);
        setDiscordId(discordId);
        setMinecraftId(minecraftId);
    }
}
