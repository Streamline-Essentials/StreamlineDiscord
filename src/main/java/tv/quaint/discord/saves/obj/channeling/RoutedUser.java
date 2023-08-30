package tv.quaint.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.savables.users.StreamlineUser;

public class RoutedUser {
    @Getter @Setter
    private StreamlineUser user;
    @Getter @Setter
    private long discordId;

    public RoutedUser(StreamlineUser user) {
        setUser(user);
        setDiscordId(0L);
    }

    public RoutedUser(long discordId) {
        setUser(null);
        setDiscordId(discordId);
    }

    public boolean isMinecraft() {
        return getUser() != null;
    }

    public boolean isDiscord() {
        return getDiscordId() != 0L;
    }
}
