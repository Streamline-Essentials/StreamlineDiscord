package tv.quaint.discordmodule.commands.given;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;

public class PingCommand extends DiscordCommand {
    @Getter @Setter
    private boolean replyEmbedded;
    @Getter @Setter
    private String replyMessage;

    public PingCommand() {
        super("ping");

        setReplyEmbedded(resource.getOrDefault("messages.reply.embedded", true));
        setReplyMessage(resource.getOrDefault("messages.reply.message", "Bot Ping: %%"));
    }

    @Override
    public void execute(MessagedString messagedString) {

    }
}
