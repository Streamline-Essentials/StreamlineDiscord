package tv.quaint.discordmodule.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

public class PingCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public PingCommand() {
        super("ping",
                "ping", "p"
        );

        setReplyMessage(getResource().getOrSetDefault("messages.reply.message", "--file:ping-response.json"));
        loadFile("ping-response.json");
    }

    @Override
    public void init() {

    }

    @Override
    public void executeMore(MessagedString messagedString) {
        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getIdLong(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getIdLong(), getReplyMessage());
        }
    }
}
