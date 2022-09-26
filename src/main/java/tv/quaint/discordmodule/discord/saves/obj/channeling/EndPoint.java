package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import org.javacord.api.entity.channel.ServerTextChannel;
import tv.quaint.discordmodule.discord.DiscordHandler;

import java.util.Optional;

public class EndPoint {
    @Getter @Setter
    private EndPointType type;
    @Getter @Setter
    private String identifier; // Server name, uuid, or permission, or room name.
    @Getter @Setter
    private String format;

    public EndPoint(EndPointType type, String identifier, String format) {
        setType(type);
        setIdentifier(identifier);
        setFormat(format);
    }

    public Optional<ServerTextChannel> asServerTextChannel() {
        if (! type.equals(EndPointType.DISCORD_TEXT)) return Optional.empty();
        try {
            long channelId = Long.parseLong(identifier);
            return DiscordHandler.getServerTextChannelById(channelId);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EndPoint o) {
            return o.getType().equals(getType()) &&
                    o.getIdentifier().equals(getIdentifier());
        } else return super.equals(obj);
    }
}
