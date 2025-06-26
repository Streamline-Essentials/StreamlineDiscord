package host.plas.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import host.plas.bukkit.events.streamline.EventingEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class BukkitListener implements Listener {
    @EventHandler
    public void onEvent(PlayerAdvancementDoneEvent event) {
        EventingEvent<Event> eventingEvent = new EventingEvent<>(event.getEventName(), event);
        eventingEvent.fire();
    }

    @EventHandler
    public void onEvent(PlayerDeathEvent event) {
        EventingEvent<Event> eventingEvent = new EventingEvent<>(event.getEventName(), event);
        eventingEvent.fire();
    }
}
