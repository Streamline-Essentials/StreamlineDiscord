package host.plas.events.streamline.routes;

import host.plas.discord.saves.obj.channeling.Route;

public class DiscordRouteCreateEventUnsure<T extends Route> extends DiscordRouteEvent<T> {
    public DiscordRouteCreateEventUnsure(T route) {
        super(route);
    }
}
