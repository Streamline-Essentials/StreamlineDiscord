package tv.quaint.discordmodule.server;

import net.streamline.api.events.StreamlineEvent;


public abstract class DSLServerEvent<T extends StreamlineEvent> extends ServerEvent<T> {
    public DSLServerEvent(String identifier) {
        super(identifier);
    }
}
