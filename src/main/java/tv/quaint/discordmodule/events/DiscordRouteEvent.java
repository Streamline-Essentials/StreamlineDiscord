package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.channeling.ChanneledFolder;
import tv.quaint.discordmodule.discord.saves.obj.channeling.Route;

public class DiscordRouteEvent<T extends Route> extends ModuleEvent {
    @Getter @Setter
    private T route;

    public DiscordRouteEvent(T route) {
        super(DiscordModule.getInstance());
        setRoute(route);
    }
}
