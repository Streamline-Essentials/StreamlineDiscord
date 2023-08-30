package tv.quaint.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import tv.quaint.discord.DiscordCommand;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;

public class HelpCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public HelpCommand() {
        super("help",
                "help", "h"
        );

        setReplyMessage(getResource().getOrSetDefault("messages.reply.message", "--file:help-response.json"));
        loadFile("help-response.json");
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
        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            return DiscordMessenger.simpleEmbed(ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            return DiscordMessenger.simpleMessage(getReplyMessage());
        }
    }
}
