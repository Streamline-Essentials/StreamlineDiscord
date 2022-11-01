package tv.quaint.discordmodule.config;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.BaseRunnable;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.events.VerificationCompleteEvent;
import tv.quaint.storage.documents.SimpleJsonDocument;
import tv.quaint.storage.resources.flat.FlatFileResource;
import tv.quaint.thebase.lib.leonhard.storage.Json;

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

    public void verifyUser(String uuid, long discordId) {
        ConcurrentSkipListSet<Long> r = getDiscordIdsOf(uuid);
        r.add(discordId);
        getVerifiedUsers().put(uuid, r);
        write("users." + uuid + ".identifiers", r.stream().toList());
        setPreferredDiscord(uuid, discordId);
        ModuleUtils.fireEvent(new VerificationCompleteEvent(discordId, uuid, DiscordHandler.getOrGetVerification(uuid)));
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

    public String discordIdToUUID(long discordId) {
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
