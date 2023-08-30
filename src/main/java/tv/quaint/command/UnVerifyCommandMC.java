package tv.quaint.command;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.DiscordModule;
import tv.quaint.events.verification.off.UnVerificationAlreadyUnVerifiedEvent;
import tv.quaint.events.verification.off.UnVerificationSuccessEvent;

import java.util.concurrent.ConcurrentSkipListSet;

public class UnVerifyCommandMC extends ModuleCommand {
    String messageUnverified;
    String messageVerified;

    public UnVerifyCommandMC() {
        super(DiscordModule.getInstance(),
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
    public void run(StreamlineUser streamlineUser, String[] strings) {
        if (DiscordModule.getVerifiedUsers().isVerified(streamlineUser)) {
            ModuleUtils.sendMessage(streamlineUser, messageVerified);
            long id = DiscordModule.getVerifiedUsers().getDiscordIdsOf(streamlineUser.getUuid()).first();
            DiscordModule.getVerifiedUsers().unverifyUser(streamlineUser);
            new UnVerificationSuccessEvent(true, id, streamlineUser.getUuid()).fire();
        } else {
            new UnVerificationAlreadyUnVerifiedEvent(true).fire();
            ModuleUtils.sendMessage(streamlineUser, messageUnverified);
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser streamlineUser, String[] strings) {
        return new ConcurrentSkipListSet<>();
    }
}
