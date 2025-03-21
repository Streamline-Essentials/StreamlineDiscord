package host.plas.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import host.plas.bukkit.events.streamline.EventingEvent;

public class BukkitListener implements Listener {
    @EventHandler
    public <E extends Event> void onEvent(E ev) {
        EventingEvent<E> event = new EventingEvent<>(ev.getEventName(), ev);
        event.fire();
    }
}
