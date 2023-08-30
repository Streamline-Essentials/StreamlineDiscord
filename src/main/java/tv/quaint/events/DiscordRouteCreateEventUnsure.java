package tv.quaint.events;

import tv.quaint.discord.saves.obj.channeling.Route;

public class DiscordRouteCreateEventUnsure<T extends Route> extends DiscordRouteEvent<T> {
    public DiscordRouteCreateEventUnsure(T route) {
        super(route);
    }
}
