package tv.quaint.discordmodule.discord.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.BotMessageConfig;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

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
        if (! messagedString.hasCommandArgs()) {
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().failureAlreadyVerifiedDiscord());
        } else {
            return DiscordHandler.tryVerificationForUser(messagedString, messagedString.getCommandArgsStringed(), true);
        }
    }
}
