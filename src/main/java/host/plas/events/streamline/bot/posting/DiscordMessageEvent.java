package host.plas.events.streamline.bot.posting;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.discord.MessagedString;
import singularity.events.modules.ModuleEvent;

@Setter
@Getter
public class DiscordMessageEvent extends ModuleEvent {
    private MessagedString message;

    public DiscordMessageEvent(MessagedString message) {
        super(DiscordModule.getInstance());
        setMessage(message);
    }
}
