package tv.quaint.discordmodule.events.serverevents;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.interfaces.ModuleLike;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;

import java.util.function.Consumer;
import java.util.function.Function;

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
