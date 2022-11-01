package tv.quaint.discordmodule.discord.saves.obj.channeling;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.events.DiscordEventRouteCreateEvent;
import tv.quaint.discordmodule.server.ServerEvent;

public class ServerEventRoute<T extends ServerEvent<?>> extends Route {
    @Getter @Setter
    private T event;
    @Getter @Setter
    private int subscribeIndex;

    public ServerEventRoute(String uuid, ChanneledFolder folder) {
        super(uuid, folder, true);
    }

    public ServerEventRoute(EndPoint input, ChanneledFolder folder, T event) {
        super(input, new EndPoint(EndPointType.DISCORD_TEXT, folder.getIdentifier().split("-", 2)[1],
                event.getDefaultMessageFormat("")), folder, true);
        if (event.isEnabled()) {
            int i = event.subscribe(
                    () -> getOutput().getToFormat(),
                    (e) -> {
                        bounceMessage(new RoutedUser(ModuleUtils.getConsole()), e);
                        return true;
                    }
            );
            setSubscribeIndex(i);
        } else {
            setSubscribeIndex(-1);
        }

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
