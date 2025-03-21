package host.plas.events.streamline.routes;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.discord.saves.obj.channeling.Route;
import singularity.events.modules.ModuleEvent;

@Setter
@Getter
public class DiscordRouteEvent<T extends Route> extends ModuleEvent {
    private T route;

    public DiscordRouteEvent(T route) {
        super(DiscordModule.getInstance());
        setRoute(route);
    }
}
