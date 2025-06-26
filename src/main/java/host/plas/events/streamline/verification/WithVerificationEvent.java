package host.plas.events.streamline.verification;

import host.plas.utils.DiscordUtils;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import singularity.data.console.CosmicSender;
import singularity.data.players.CosmicPlayer;
import singularity.utils.UserUtils;

import java.util.Optional;

public class WithVerificationEvent extends VerificationEvent {
    @Nullable
    private String uuid;
    @Nullable
    private Long discordId;

    public WithVerificationEvent(@Nullable String uuid, @Nullable Long discordId, boolean isFromCommand) {
        super(isFromCommand);
        this.uuid = uuid;
        this.discordId = discordId;
    }

    public Optional<CosmicSender> getSender() {
        if (uuid == null) return Optional.empty();
        return UserUtils.getOrGetSender(uuid);
    }

    public Optional<CosmicPlayer> getPlayer() {
        if (uuid == null) return Optional.empty();
        return UserUtils.getOrCreatePlayer(uuid);
    }

    public Optional<User> getDiscordUser() {
        return DiscordUtils.getUserById(discordId);
    }

    public boolean hasMinecraftUuid() {
        return uuid != null;
    }

    public boolean hasDiscordId() {
        return discordId != null;
    }
}
