package tv.quaint.discordmodule.events;

import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;

public class DiscordRouteCreateEventSure extends DiscordRouteCreateEventUnsure<Route> {
    public DiscordRouteCreateEventSure(Route route) {
        super(route);
    }
}
