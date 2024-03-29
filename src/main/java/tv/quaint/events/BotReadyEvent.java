package tv.quaint.events;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;

public class BotReadyEvent extends ModuleEvent {
    @Getter
    private final User bot;

    public BotReadyEvent(final User bot) {
        super(DiscordModule.getInstance());

        this.bot = bot;
    }
}
