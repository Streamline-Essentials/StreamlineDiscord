package host.plas.events.streamline.bot;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import host.plas.DiscordModule;
import singularity.events.modules.ModuleEvent;

@Getter
public class BotReadyEvent extends ModuleEvent {
    private final User bot;

    public BotReadyEvent(final User bot) {
        super(DiscordModule.getInstance());

        this.bot = bot;
    }
}
