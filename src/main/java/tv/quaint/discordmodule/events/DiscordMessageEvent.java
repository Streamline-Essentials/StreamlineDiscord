package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.modules.StreamlineModule;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

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
