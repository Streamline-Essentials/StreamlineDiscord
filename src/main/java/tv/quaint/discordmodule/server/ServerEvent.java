package tv.quaint.discordmodule.server;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.server.events.DiscordEventMessageBuilder;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;

public abstract class ServerEvent<T> {
    @Getter
    private final String identifier;
    @Getter @Setter
    private boolean enabled = false;

    public ServerEvent(String identifier) {
        this.identifier = identifier;
    }

    abstract public void onEvent(T event);

    public abstract void pushEvents(T event);

    public void forwardMessage(EventMessageInfo eventMessageInfo) {
        if (UserUtils.getOnlinePlayers().size() == 0) return;
        StreamlinePlayer player = UserUtils.getOnlinePlayers().firstEntry().getValue();
        if (player == null) return;

        DiscordModule.getInstance().logDebug("Forwarding message to next server: " + eventMessageInfo.read());

        DiscordEventMessageBuilder.build(eventMessageInfo, player).send();
    }
}
