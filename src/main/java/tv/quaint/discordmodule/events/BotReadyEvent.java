package tv.quaint.discordmodule.events;

import discord4j.core.object.entity.User;
import lombok.Getter;
import net.streamline.api.events.StreamlineEvent;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.modules.StreamlineModule;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;

public class BotReadyEvent extends ModuleEvent {
    @Getter
    private final User bot;

    public BotReadyEvent(final User bot) {
        super(DiscordModule.getInstance());

        this.bot = bot;
    }
}
