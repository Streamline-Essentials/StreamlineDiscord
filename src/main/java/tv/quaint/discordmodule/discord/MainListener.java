package tv.quaint.discordmodule.discord;

import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.events.DiscordCommandEvent;
import tv.quaint.discordmodule.events.DiscordMessageEvent;

public class MainListener implements StreamlineListener {
    public MainListener() {
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @EventProcessor
    public void DiscordMessageEvent(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (! event.getMessage().hasPrefix()) {
            return;
        }

        DiscordCommand command = DiscordHandler.getCommandByAlias(event.getMessage().getBase());
        if (command == null) {
            DiscordModule.getInstance().logWarning("Could not get DiscordCommand with alias of '" + event.getMessage().getBase() + "'.");
            return;
        }

        ModuleUtils.fireEvent(new DiscordCommandEvent(event.getMessage(), command));
    }

    @EventProcessor
    public void DiscordMessageEvent(DiscordCommandEvent event) {
        DiscordModule.getInstance().logInfo("Executing command '" + event.getCommand().getCommandIdentifier() + "'...!");
        event.getCommand().execute(event.getMessage());
    }
}
