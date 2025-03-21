package host.plas.bukkit.events.streamline;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import org.bukkit.event.Event;
import host.plas.DiscordModule;

@Setter
@Getter
public class EventingEvent<T extends Event> extends ModuleEvent {
    String identifier;
    T ev;

    public EventingEvent(String identifier, T ev) {
        super(DiscordModule.getInstance());
        setIdentifier(identifier);
        setEv(ev);
    }
}
