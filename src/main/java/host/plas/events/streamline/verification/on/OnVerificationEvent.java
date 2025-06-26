package host.plas.events.streamline.verification.on;

import host.plas.discord.MessagedString;
import host.plas.events.streamline.verification.VerificationResultEvent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class OnVerificationEvent extends VerificationResultEvent {
    private MessagedString message;
    private String verification;

    public OnVerificationEvent(MessagedString message, String verification, @Nullable String uuid, Result result, boolean isFromCommand) {
        super(uuid, message.getAuthor().getIdLong(), result, isFromCommand);

        this.message = message;
        this.verification = verification;
    }
}
