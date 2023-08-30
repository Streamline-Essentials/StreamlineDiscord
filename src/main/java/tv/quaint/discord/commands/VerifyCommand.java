package tv.quaint.discord.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.utils.UserUtils;
import tv.quaint.DiscordModule;
import tv.quaint.discord.DiscordHandler;
import tv.quaint.discord.DiscordCommand;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;

public class VerifyCommand extends DiscordCommand {
    public VerifyCommand() {
        super("verify",
                "ver", "v"
        );
    }

    @Override
    public void init() {
    }

    @Override
    public CommandCreateAction setupOptionData(CommandCreateAction action) {
        return action.addOption(OptionType.STRING, "code", "The code you received from the bot.", true);
    }

    @Override
    public SingleSet<MessageCreateData, BotMessageConfig> executeMore(MessagedString messagedString) {
        setReplyEphemeral(DiscordModule.getConfig().verificationResponsesPrivate());
        if (! messagedString.hasCommandArgs()) {
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().verifiedFailureGenericDiscord());
        } else {
            return DiscordHandler.tryVerificationForUser(messagedString, messagedString.getCommandArgsStringed(), true);
        }
    }
}
