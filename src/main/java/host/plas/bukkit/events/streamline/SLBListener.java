package host.plas.bukkit.events.streamline;

import org.bukkit.event.Event;
import host.plas.bukkit.events.EventHandler;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

public class SLBListener implements BaseEventListener {
    @BaseProcessor
    public <E extends Event> void onEvent(EventingEvent<E> ev) {
        EventHandler.handle(ev.getEventName(), ev.getEv());
    }
}
