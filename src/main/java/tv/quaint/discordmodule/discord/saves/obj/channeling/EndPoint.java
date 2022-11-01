package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;
import tv.quaint.discordmodule.discord.DiscordHandler;

import java.util.Optional;

public class EndPoint {
    @Getter @Setter
    private EndPointType type;
    @Getter @Setter
    private String identifier; // Server name, uuid, or permission, or room name.
    @Getter @Setter
    private String toFormat;

    public EndPoint(EndPointType type, String identifier, String toFormat) {
        setType(type);
        setIdentifier(identifier);
        setToFormat(toFormat);
    }

    public TextChannel asServerTextChannel() {
        if (! type.equals(EndPointType.DISCORD_TEXT)) return null;
        try {
            long channelId = Long.parseLong(identifier);
            return DiscordHandler.getTextChannelById(channelId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
