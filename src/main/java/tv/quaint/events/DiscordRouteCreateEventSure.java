package tv.quaint.events;

import tv.quaint.discord.saves.obj.channeling.Route;

public class DiscordRouteCreateEventSure extends DiscordRouteCreateEventUnsure<Route> {
    public DiscordRouteCreateEventSure(Route route) {
        super(route);
    }
}
