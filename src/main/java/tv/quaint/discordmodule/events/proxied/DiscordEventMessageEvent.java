package tv.quaint.discordmodule.events.proxied;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.interfaces.ModuleLike;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.server.ServerEvent;

public class DiscordEventMessageEvent<T extends ServerEvent<?>> extends ModuleEvent {
    @Getter
    final T event;
    @Getter @Setter
    boolean willSend = true;
    @Getter @Setter
    boolean hasBeenSent = false;

    public DiscordEventMessageEvent(T event) {
        super(DiscordModule.getInstance());
        this.event = event;
    }

    public void send() {

    }
}
