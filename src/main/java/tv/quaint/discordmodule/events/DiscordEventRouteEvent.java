package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ServerEventRoute;

public class DiscordEventRouteEvent extends DiscordRouteEvent<ServerEventRoute<?>> {
    public DiscordEventRouteEvent(ServerEventRoute<?> route) {
        super(route);
    }
}
