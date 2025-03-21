package host.plas.events.streamline.verification;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import host.plas.DiscordModule;
import host.plas.discord.DiscordHandler;
import host.plas.discord.MessagedString;
import singularity.data.console.CosmicSender;
import singularity.events.modules.ModuleEvent;
import singularity.utils.UserUtils;

@Setter
@Getter
public class VerificationResultEvent extends ModuleEvent {
    public enum Result {
        VERIFIED_SUCCESS,
        VERIFIED_FAILURE,

        UNVERIFIED_SUCCESS,
        UNVERIFIED_FAILURE,

        ALREADY_VERIFIED,
        ALREADY_UNVERIFIED,
        ;
    }

    private MessagedString message;
    @Nullable
    private String streamlineUUID;
    private String verification;
    private Result result;
    private boolean fromCommand;

    public VerificationResultEvent(MessagedString message, @Nullable String streamlineUUID, String verification, Result result, boolean isFromCommand) {
        super(DiscordModule.getInstance());
        setMessage(message);
        setStreamlineUUID(streamlineUUID);
        setVerification(verification);
        setResult(result);
        setFromCommand(isFromCommand);
    }

    private CosmicSender uuidAsUser() {
        return UserUtils.getOrCreateSender(getStreamlineUUID());
    }

    private User idAsUser() {
        return DiscordHandler.getUser(getMessage().getAuthor().getIdLong());
    }

    public boolean hasStreamlineUUID() {
        return getStreamlineUUID() != null;
    }
}
