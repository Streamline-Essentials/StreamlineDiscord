package tv.quaint.discordmodule.events.verification;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.jetbrains.annotations.Nullable;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;

public class VerificationResultEvent extends ModuleEvent {
    public enum Result {
        SUCCESS,
        FAILURE,
        ALREADY_VERIFIED,
        ;
    }

    @Getter
    @Setter
    private MessagedString message;
    @Getter @Setter @Nullable
    private String streamlineUUID;
    @Getter @Setter
    private String verification;
    @Getter @Setter
    private Result result;
    @Getter @Setter
    private boolean fromCommand;

    public VerificationResultEvent(MessagedString message, @Nullable String streamlineUUID, String verification, Result result, boolean isFromCommand) {
        super(DiscordModule.getInstance());
        setMessage(message);
        setStreamlineUUID(streamlineUUID);
        setVerification(verification);
        setResult(result);
        setFromCommand(isFromCommand);
    }

    private StreamlineUser uuidAsUser() {
        return ModuleUtils.getOrGetUser(getStreamlineUUID());
    }

    private User idAsUser() {
        return DiscordHandler.getUser(getMessage().getAuthor().getIdLong());
    }

    public boolean hasStreamlineUUID() {
        return getStreamlineUUID() != null;
    }
}
