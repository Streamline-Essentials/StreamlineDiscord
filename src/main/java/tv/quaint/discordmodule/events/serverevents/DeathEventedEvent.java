package tv.quaint.discordmodule.events.serverevents;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.function.Consumer;
import java.util.function.Function;

public class DeathEventedEvent extends ServerEventedEvent<PlayerDeathEvent>{
    public DeathEventedEvent(String identifier, PlayerDeathEvent ev) {
        super(identifier, ev);
    }
}
