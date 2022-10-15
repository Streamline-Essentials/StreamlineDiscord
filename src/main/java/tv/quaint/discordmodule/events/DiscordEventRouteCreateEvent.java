package tv.quaint.discordmodule.events;

import tv.quaint.discordmodule.discord.saves.obj.channeling.ServerEventRoute;

public class DiscordEventRouteCreateEvent extends DiscordRouteCreateEventUnsure<ServerEventRoute<?>> {
    public DiscordEventRouteCreateEvent(ServerEventRoute<?> route) {
        super(route);
    }
}
