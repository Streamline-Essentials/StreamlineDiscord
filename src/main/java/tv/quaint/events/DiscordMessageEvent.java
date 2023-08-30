package tv.quaint.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.DiscordMessenger;

public class DiscordMessageEvent extends ModuleEvent {
    @Getter @Setter
    private MessagedString message;

    public DiscordMessageEvent(MessagedString message) {
        super(DiscordModule.getInstance());
        setMessage(message);

        if (message.getAuthor().isBot()) DiscordMessenger.incrementMessageCountInBots();
        else DiscordMessenger.incrementMessageCountIn();
    }
}
