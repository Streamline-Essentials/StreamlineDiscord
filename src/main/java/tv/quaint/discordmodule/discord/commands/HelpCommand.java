package tv.quaint.discordmodule.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

public class HelpCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public HelpCommand() {
        super("help",
                "help", "h"
        );

        setReplyMessage(resource.getOrSetDefault("messages.reply.message", "--file:help-response.json"));
        loadFile("help-response.json");
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
