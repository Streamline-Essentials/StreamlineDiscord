package tv.quaint.discordmodule.events.serverevents;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.function.Function;

public class AdvancementEventedEvent extends ServerEventedEvent<PlayerAdvancementDoneEvent>{
    public AdvancementEventedEvent(String identifier, PlayerAdvancementDoneEvent ev) {
        super(identifier, ev);
    }
}
