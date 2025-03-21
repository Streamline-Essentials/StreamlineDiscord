package host.plas.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import host.plas.DiscordModule;
import host.plas.discord.DiscordHandler;
import singularity.loading.Loadable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class EndPoint implements Loadable<EndPoint> {
    private String identifier;

    private EndPointType type;
    private String endPointIdentifier; // Server name, uuid, or permission, or room name.
    private String toFormat;

    public EndPoint(String identifier) {
        this.identifier = identifier;
    }

    public EndPoint() {
        this(UUID.randomUUID().toString());
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
        if (obj instanceof EndPoint) {
            EndPoint o = (EndPoint) obj;
            return o.getType().equals(getType()) &&
                    o.getIdentifier().equals(getIdentifier());
        } else return super.equals(obj);
    }

    @Override
    public void save() {
        DiscordModule.getEndPointKeeper().save(this);
    }

    @Override
    public EndPoint augment(CompletableFuture<Optional<EndPoint>> completableFuture) {
        CompletableFuture.runAsync(() -> {
            Optional<EndPoint> optional = completableFuture.join();
            if (optional.isEmpty()) return;
            EndPoint endPoint = optional.get();

            this.setIdentifier(endPoint.getIdentifier());
            this.setType(endPoint.getType());
            this.setEndPointIdentifier(endPoint.getEndPointIdentifier());
            this.setToFormat(endPoint.getToFormat());
        });

        return this;
    }

    public void drop() {
        DiscordModule.getEndPointKeeper().drop(this);
    }
}
