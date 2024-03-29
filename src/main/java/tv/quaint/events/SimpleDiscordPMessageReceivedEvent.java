package tv.quaint.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;
import tv.quaint.discord.messaging.DiscordProxiedMessage;

public class SimpleDiscordPMessageReceivedEvent extends ModuleEvent {
    @Getter @Setter
    private DiscordProxiedMessage message;

    public SimpleDiscordPMessageReceivedEvent(DiscordProxiedMessage message) {
        super(DiscordModule.getInstance());
        setMessage(message);
    }

    public String simplyGetMessage() {
        return getMessage().getMessage();
    }

    public String simplyGetInputType() {
        return getMessage().getInputType();
    }

    public String simplyGetInputIdentifier() {
        return getMessage().getInputIdentifer();
    }
}
