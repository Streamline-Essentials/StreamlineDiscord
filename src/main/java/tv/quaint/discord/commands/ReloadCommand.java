package tv.quaint.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.command.CommandHandler;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.modules.ModuleManager;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import tv.quaint.discord.DiscordCommand;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;

public class ReloadCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public ReloadCommand() {
        super("reload",
                -1L,
                "reload", "rel"
        );

        setReplyMessage(getResource().getOrSetDefault("messages.reply.message", "--file:reload-response.json"));
        loadFile("reload-response.json");
    }

    @Override
    public void init() {

    }

    @Override
    public CommandCreateAction setupOptionData(CommandCreateAction action) {
        return action;
    }

    @Override
    public SingleSet<MessageCreateData, BotMessageConfig> executeMore(MessagedString messagedString) {
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
            return DiscordMessenger.simpleEmbed(ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            return DiscordMessenger.simpleMessage(getReplyMessage());
        }
    }
}
