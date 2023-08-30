package tv.quaint.config;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.SingleSet;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.BaseRunnable;
import net.streamline.thebase.lib.leonhard.storage.Json;
import tv.quaint.storage.resources.flat.FlatFileResource;
import tv.quaint.DiscordModule;
import tv.quaint.discord.MessagedString;
import tv.quaint.discord.messaging.BotMessageConfig;
import tv.quaint.discord.messaging.DiscordMessenger;
import tv.quaint.events.verification.on.VerificationFailureEvent;
import tv.quaint.events.verification.on.VerificationSuccessEvent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class VerifiedUsers extends FlatFileResource<Json> {
    public class Runner extends BaseRunnable {
        public Runner() {
            super(1200, 1200);
        }

        @Override
        public void run() {
            setVerifiedUsers(new ConcurrentSkipListMap<>());
            setPreferredDiscord(new ConcurrentSkipListMap<>());

            ModuleUtils.getLoadedUsersSet().forEach(a -> {
                loadDiscordIdsOf(a.getUuid());
                loadPreferredDiscordOf(a.getUuid());
            });
        }
    }

    @Getter @Setter
    private ConcurrentSkipListMap<String, ConcurrentSkipListSet<Long>> verifiedUsers = new ConcurrentSkipListMap<>();
    @Getter @Setter
    private ConcurrentSkipListMap<String, Long> preferredDiscord = new ConcurrentSkipListMap<>();

    @Getter @Setter
    private Runner runner;

    public VerifiedUsers() {
        super(Json.class, "verified-users.json", DiscordModule.getInstance().getDataFolder(), false);

        setRunner(new Runner());
    }

    public SingleSet<MessageCreateData, BotMessageConfig> verifyUser(String uuid, MessagedString messagedString, String verification, boolean fromCommand) {
        ConcurrentSkipListSet<Long> r = getDiscordIdsOf(uuid);
        r.add(messagedString.getAuthor().getIdLong());
        getVerifiedUsers().put(uuid, r);
        write("users." + uuid + ".identifiers", new ArrayList<>(r));
        setPreferredDiscord(uuid, messagedString.getAuthor().getIdLong());

        StreamlineUser user = ModuleUtils.getOrGetUser(uuid);
        if (user == null) {
            new VerificationFailureEvent(messagedString, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(ModuleUtils.getConsole(), DiscordModule.getMessages().verifiedFailureGenericDiscord());
        } else {
            new VerificationSuccessEvent(messagedString, uuid, verification, fromCommand).fire();
            return DiscordMessenger.verificationMessage(user, DiscordModule.getMessages().verifiedSuccessDiscord());
        }
    }

    public void unverifyUser(long discordId) {
        StreamlineUser user = ModuleUtils.getOrGetUser(getUUIDfromDiscordID(discordId));
        if (user == null) return;

        unverifyUser(user.getUuid());
    }

    public void unverifyUser(StreamlineUser streamlineUser) {
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

    public boolean isVerified(StreamlineUser user) {
        return isVerified(user.getUuid());
    }

    public boolean isVerified(String uuid) {
        for (String key : getResource().singleLayerKeySet("users")) {
            if (key.equals(uuid)) return true;
        }

        return false;
    }
}
