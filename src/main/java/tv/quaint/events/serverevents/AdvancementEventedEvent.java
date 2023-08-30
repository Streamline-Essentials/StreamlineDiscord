package tv.quaint.events.serverevents;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementEventedEvent extends ServerEventedEvent<PlayerAdvancementDoneEvent>{
    public AdvancementEventedEvent(String identifier, PlayerAdvancementDoneEvent ev) {
        super(identifier, ev);
    }
}
