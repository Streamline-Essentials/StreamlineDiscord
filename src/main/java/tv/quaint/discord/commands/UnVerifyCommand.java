package tv.quaint.discord.commands;

import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.utils.UserUtils;
import tv.quaint.DiscordModule;
import tv.quaint.discord.DiscordCommand;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;
import tv.quaint.events.verification.off.UnVerificationAlreadyUnVerifiedEvent;
import tv.quaint.events.verification.off.UnVerificationFailureEvent;
import tv.quaint.events.verification.off.UnVerificationSuccessEvent;

public class UnVerifyCommand extends DiscordCommand {
    public UnVerifyCommand() {
        super("unverify",
                "unver", "uv"
        );
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
        setReplyEphemeral(DiscordModule.getConfig().verificationResponsesPrivate());
        if (! messagedString.hasCommandArgs()) {
            new UnVerificationFailureEvent(true).fire();
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().unVerifiedFailureGenericDiscord());
        } else {
            if (DiscordModule.getVerifiedUsers().isVerified(messagedString.getAuthor().getIdLong())) {
                String uuid = DiscordModule.getVerifiedUsers().getUUIDfromDiscordID(messagedString.getAuthor().getIdLong());
                DiscordModule.getVerifiedUsers().unverifyUser(messagedString.getAuthor().getIdLong());

                new UnVerificationSuccessEvent(true, messagedString.getAuthor().getIdLong(), uuid).fire();
                return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().unVerifiedSuccessDiscord());
            } else {
                new UnVerificationAlreadyUnVerifiedEvent(true).fire();
                return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().unVerifiedFailureAlreadyUnVerifiedDiscord());
            }
        }
    }
}
