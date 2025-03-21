package host.plas.discord.commands;

import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import singularity.objects.SingleSet;
import singularity.utils.UserUtils;
import host.plas.DiscordModule;
import host.plas.discord.DiscordCommand;
import host.plas.discord.MessagedString;
import host.plas.discord.messaging.BotMessageConfig;
import host.plas.discord.messaging.DiscordMessenger;
import host.plas.events.streamline.verification.off.UnVerificationAlreadyUnVerifiedEvent;
import host.plas.events.streamline.verification.off.UnVerificationFailureEvent;
import host.plas.events.streamline.verification.off.UnVerificationSuccessEvent;

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
