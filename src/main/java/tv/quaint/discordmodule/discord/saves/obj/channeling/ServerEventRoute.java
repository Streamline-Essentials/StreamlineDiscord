package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.events.DiscordEventRouteCreateEvent;
import tv.quaint.discordmodule.server.ServerEvent;

public class ServerEventRoute<T extends ServerEvent<?>> extends Route {
    @Getter @Setter
    private T event;

    public ServerEventRoute(String uuid, ChanneledFolder folder) {
        super(uuid, folder, true);
    }

    public ServerEventRoute(EndPoint input, ChanneledFolder folder, T event) {
        super(input, new EndPoint(EndPointType.DISCORD_TEXT, folder.getIdentifier().split("-", 2)[1],
                ""), folder, true);
        this.event = event;

        new DiscordEventRouteCreateEvent(this).fire();
    }

    @Override
    public void remove() {
        if (getParent() != null) getParent().unloadRoute(getUuid());

        try {
            getStorageResource().delete();
            dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
