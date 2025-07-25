package host.plas.command;

import host.plas.StreamlineDiscord;
import host.plas.config.VerifiedUsers;
import host.plas.events.streamline.verification.off.UnVerificationAlreadyUnVerifiedEvent;
import host.plas.events.streamline.verification.off.UnVerificationSuccessEvent;
import singularity.command.CosmicCommand;
import singularity.command.ModuleCommand;
import singularity.command.context.CommandContext;
import singularity.data.console.CosmicSender;
import singularity.modules.ModuleUtils;

import java.util.concurrent.ConcurrentSkipListSet;

public class UnVerifyCommandMC extends ModuleCommand {
    String messageUnverified;
    String messageVerified;

    public UnVerifyCommandMC() {
        super(StreamlineDiscord.getInstance(),
                "unverify",
                "streamline.command.unverify.default",
                "duv"
        );

        messageUnverified = getCommandResource().getOrSetDefault("messages.reply.unverified",
                "&eYou are already unverified with &9&lDiscord&8!");
        messageVerified = getCommandResource().getOrSetDefault("messages.reply.verified",
                "&eTrying to unverify you with &9&lDiscord&8...");
    }

    @Override
    public void run(CommandContext<CosmicCommand> context) {
        CosmicSender streamlineUser = context.getSender();

        if (VerifiedUsers.isVerified(streamlineUser)) {
            ModuleUtils.sendMessage(streamlineUser, messageVerified);
            long id = VerifiedUsers.getDiscordIdsOf(streamlineUser.getUuid()).first();
            VerifiedUsers.unverifyUser(streamlineUser);
            new UnVerificationSuccessEvent(true, streamlineUser.getUuid(), id, null, null).fire();
        } else {
            new UnVerificationAlreadyUnVerifiedEvent(true, streamlineUser.getUuid(), null, null, null).fire();
            ModuleUtils.sendMessage(streamlineUser, messageUnverified);
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(CommandContext<CosmicCommand> context) {
        return new ConcurrentSkipListSet<>();
    }
}
