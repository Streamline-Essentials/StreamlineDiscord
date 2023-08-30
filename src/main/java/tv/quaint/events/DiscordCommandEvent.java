package tv.quaint.events;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.discord.DiscordCommand;
import tv.quaint.discord.MessagedString;

public class DiscordCommandEvent extends DiscordMessageEvent {
    @Getter @Setter
    private DiscordCommand command;

    public DiscordCommandEvent(MessagedString message, DiscordCommand command) {
        super(message);
        setCommand(command);
    }
}
