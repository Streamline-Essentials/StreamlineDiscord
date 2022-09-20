package tv.quaint.discordmodule.commands.given;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.CommandHandler;
import net.streamline.api.command.StreamlineCommand;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.modules.ModuleManager;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.commands.DiscordCommand;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

import java.util.ArrayList;
import java.util.Iterator;

public class ReloadCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public ReloadCommand() {
        super("reload",
                -1L,
                "reload", "rel"
        );

        setReplyMessage(resource.getOrDefault("messages.reply.message", "--file:reload-response.json"));
    }

    @Override
    public void executeMore(MessagedString messagedString) {
        GivenConfigs.getMainConfig().reloadResource(true);
        GivenConfigs.getMainMessages().reloadResource(true);

        CommandHandler.getLoadedStreamlineCommands().forEach((s, command) -> {
            CommandHandler.unregisterStreamlineCommand(command);
            command.getCommandResource().reloadResource(true);
            command.getCommandResource().syncCommand();
            CommandHandler.registerStreamlineCommand(command);
        });

        ModuleManager.restartModules();

        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getId(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getId(), getReplyMessage());
        }
    }
}
