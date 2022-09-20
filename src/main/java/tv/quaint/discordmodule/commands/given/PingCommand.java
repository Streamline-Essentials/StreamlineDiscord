package tv.quaint.discordmodule.commands.given;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

public class PingCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public PingCommand() {
        super("ping",
                "ping", "p"
        );

        setReplyMessage(resource.getOrDefault("messages.reply.message", "--file:ping-response.json"));
    }

    @Override
    public void executeMore(MessagedString messagedString) {
        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getId(), getReplyMessage());
        }
    }
}
