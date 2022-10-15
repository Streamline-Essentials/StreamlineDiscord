package tv.quaint.discordmodule.events;

import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;

public class DiscordRouteCreateEventUnsure<T extends Route> extends DiscordRouteEvent<T> {
    public DiscordRouteCreateEventUnsure(T route) {
        super(route);
    }
}
