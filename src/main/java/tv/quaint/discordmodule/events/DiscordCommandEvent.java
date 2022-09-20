package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;

public class DiscordCommandEvent extends DiscordMessageEvent {
    @Getter @Setter
    private DiscordCommand command;

    public DiscordCommandEvent(MessagedString message, DiscordCommand command) {
        super(message);
        setCommand(command);
    }
}
