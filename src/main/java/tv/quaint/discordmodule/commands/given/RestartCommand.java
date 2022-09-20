package tv.quaint.discordmodule.commands.given;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.CommandHandler;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.modules.ModuleManager;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

public class RestartCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public RestartCommand() {
        super("restart",
                -1L,
                "restart", "res"
        );

        setReplyMessage(resource.getOrDefault("messages.reply.message", "--file:restart-response.json"));
    }

    @Override
    public void executeMore(MessagedString messagedString) {
        DiscordHandler.init();

        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getId(), getReplyMessage());
        }
    }
}
