package tv.quaint.events.serverevents;

import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEventedEvent extends ServerEventedEvent<PlayerDeathEvent>{
    public DeathEventedEvent(String identifier, PlayerDeathEvent ev) {
        super(identifier, ev);
    }
}
