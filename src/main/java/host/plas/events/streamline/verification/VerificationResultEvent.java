package host.plas.events.streamline.verification;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class VerificationResultEvent extends WithVerificationEvent {
    public enum Result {
        VERIFIED_SUCCESS,
        VERIFIED_FAILURE,

        UNVERIFIED_SUCCESS,
        UNVERIFIED_FAILURE,

        ALREADY_VERIFIED,
        ALREADY_UNVERIFIED,
        ;
    }

    private Result result;

    public VerificationResultEvent( @Nullable String uuid, @Nullable Long discordId, Result result, boolean isFromCommand) {
        super(uuid, discordId, isFromCommand);
        this.result = result;
    }
}
