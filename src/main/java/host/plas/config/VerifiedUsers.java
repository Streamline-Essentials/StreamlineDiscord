package host.plas.config;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import host.plas.DiscordModule;
import host.plas.discord.MessagedString;
import host.plas.discord.messaging.BotMessageConfig;
import host.plas.discord.messaging.DiscordMessenger;
import host.plas.events.streamline.verification.on.VerificationFailureEvent;
import host.plas.events.streamline.verification.on.VerificationSuccessEvent;
import singularity.data.console.CosmicSender;
import singularity.modules.ModuleUtils;
import singularity.objects.SingleSet;
import singularity.scheduler.BaseRunnable;
import singularity.utils.UserUtils;
import tv.quaint.storage.resources.flat.simple.SimpleJson;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Setter
@Getter
public class VerifiedUsers extends SimpleJson {
    public class Runner extends BaseRunnable {
        public Runner() {
            super(1200, 1200);
        }

        @Override
        public void run() {
            setVerifiedUsers(new ConcurrentSkipListMap<>());
            setPreferredDiscord(new ConcurrentSkipListMap<>());

            ModuleUtils.getLoadedSendersSet().forEach(a -> {
                loadDiscordIdsOf(a.getUuid());
                loadPreferredDiscordOf(a.getUuid());
            });
        }
    }

    private ConcurrentSkipListMap<String, ConcurrentSkipListSet<Long>> verifiedUsers = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<String, Long> preferredDiscord = new ConcurrentSkipListMap<>();

    private Runner runner;

    public VerifiedUsers() {
        super("verified-users.json", DiscordModule.getInstance().getDataFolder(), false);

        setRunner(new Runner());
    }

    public SingleSet<MessageCreateData, BotMessageConfig> verifyUser(String uuid, MessagedString messagedString, String verification, boolean fromCommand) {
        ConcurrentSkipListSet<Long> r = getDiscordIdsOf(uuid);
        r.add(messagedString.getAuthor().getIdLong());
        getVerifiedUsers().put(uuid, r);
        write("users." + uuid + ".identifiers", new ArrayList<>(r));
        setPreferredDiscord(uuid, messagedString.getAuthor().getIdLong());

        CosmicSender user = ModuleUtils.getOrCreateSender(uuid);
        if (user == null) {
            new VerificationFailureEvent(messagedString, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(UserUtils.getConsole(), DiscordModule.getMessages().verifiedFailureGenericDiscord());
        } else {
            new VerificationSuccessEvent(messagedString, uuid, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(user, DiscordModule.getMessages().verifiedSuccessDiscord());
        }
    }

    public void unverifyUser(long discordId) {
        CosmicSender user = ModuleUtils.getOrCreateSender(getUUIDfromDiscordID(discordId));
        if (user == null) return;

        unverifyUser(user.getUuid());
    }

    public void unverifyUser(CosmicSender streamlineUser) {
        unverifyUser(streamlineUser.getUuid());
    }

    public void unverifyUser(String uuid) {
        getVerifiedUsers().remove(uuid);
        getResource().remove("users." + uuid);
    }

    public ConcurrentSkipListSet<Long> getDiscordIdsOf(String uuid) {
        ConcurrentSkipListSet<Long> r = getVerifiedUsers().get(uuid);
        if (r != null) return r;
        r = loadDiscordIdsOf(uuid);
        return r;
    }

    private ConcurrentSkipListSet<Long> loadDiscordIdsOf(String uuid) {
        reloadResource();
        ConcurrentSkipListSet<Long> r = new ConcurrentSkipListSet<>(getResource().getLongList("users." + uuid + ".identifiers"));
        getVerifiedUsers().put(uuid, r);
        return r;
    }

    public void setPreferredDiscord(String uuid, long discordId) {
        getPreferredDiscord().put(uuid, discordId);
        write("users." + uuid + ".preferred", discordId);
    }

    private long loadPreferredDiscordOf(String uuid) {
        reloadResource();
        long r = getResource().getLong("users." + uuid + ".preferred");
        if (r == 0L) return r;
        getPreferredDiscord().put(uuid, r);
        return r;
    }

    public long getOrGetPreferredDiscord(String uuid) {
        Long r = getPreferredDiscord().get(uuid);
        if (r != null) return r;
        r = loadPreferredDiscordOf(uuid);
        return r;
    }

    public String getUUIDfromDiscordID(long discordId) {
        if (discordId == 0L) return null;

        for (String key : getResource().singleLayerKeySet("users")) {
            if (getResource().getLong("users." + key + ".preferred") == discordId) return key;
        }

        return null;
    }

    public boolean isVerified(long discordId) {
        for (String key : getResource().singleLayerKeySet("users")) {
            if (getResource().getLong("users." + key + ".preferred") == discordId) return true;
        }

        return false;
    }

    public boolean isVerified(CosmicSender user) {
        return isVerified(user.getUuid());
    }

    public boolean isVerified(String uuid) {
        for (String key : getResource().singleLayerKeySet("users")) {
            if (key.equals(uuid)) return true;
        }

        return false;
    }
}
