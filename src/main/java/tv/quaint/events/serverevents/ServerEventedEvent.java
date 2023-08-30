package tv.quaint.events.serverevents;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import org.bukkit.event.Event;
import tv.quaint.DiscordModule;

public class ServerEventedEvent<T extends Event> extends ModuleEvent {
    @Getter @Setter
    String identifier;
    @Getter @Setter
    T ev;

    public ServerEventedEvent(String identifier, T ev) {
        super(DiscordModule.getInstance());
        setIdentifier(identifier);
        setEv(ev);
    }
}
