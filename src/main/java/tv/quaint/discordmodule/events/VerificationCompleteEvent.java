package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.StreamlineModule;
import net.streamline.api.savables.users.StreamlineUser;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;

public class VerificationCompleteEvent extends ModuleEvent {
    @Getter @Setter
    private long discordId;
    @Getter @Setter
    private String streamlineUUID;
    @Getter @Setter
    private String verification;

    public VerificationCompleteEvent(long discordId, String streamlineUUID, String verification) {
        super(DiscordModule.getInstance());
        setDiscordId(discordId);
        setStreamlineUUID(streamlineUUID);
        setVerification(verification);
    }

    private StreamlineUser uuidAsUser() {
        return ModuleUtils.getOrGetUser(getStreamlineUUID());
    }

    private User idAsUser() {
        return DiscordHandler.getUser(getDiscordId());
    }
}
