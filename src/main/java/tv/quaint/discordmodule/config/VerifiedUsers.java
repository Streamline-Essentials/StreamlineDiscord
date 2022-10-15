package tv.quaint.discordmodule.config;

import de.leonhard.storage.Json;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.configs.FlatFileResource;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.BaseRunnable;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.events.VerificationCompleteEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        super(DiscordModule.getInstance(), Json.class, "verified-users.json", false);

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
        resource.remove("users." + uuid);
    }

    public ConcurrentSkipListSet<Long> getDiscordIdsOf(String uuid) {
        ConcurrentSkipListSet<Long> r = getVerifiedUsers().get(uuid);
        if (r != null) return r;
        r = loadDiscordIdsOf(uuid);
        return r;
    }

    private ConcurrentSkipListSet<Long> loadDiscordIdsOf(String uuid) {
        reloadResource();
        ConcurrentSkipListSet<Long> r = new ConcurrentSkipListSet<>(resource.getLongList("users." + uuid + ".identifiers"));
        getVerifiedUsers().put(uuid, r);
        return r;
    }

    public void setPreferredDiscord(String uuid, long discordId) {
        getPreferredDiscord().put(uuid, discordId);
        write("users." + uuid + ".preferred", discordId);
    }

    private long loadPreferredDiscordOf(String uuid) {
        reloadResource();
        long r = resource.getLong("users." + uuid + ".preferred");
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

        for (String key : resource.singleLayerKeySet("users")) {
            if (resource.getLong("users." + key + ".preferred") == discordId) return key;
        }

        return null;
    }

    public boolean isVerified(long discordId) {
        for (String key : resource.singleLayerKeySet("users")) {
            if (resource.getLong("users." + key + ".preferred") == discordId) return true;
        }

        return false;
    }

    public boolean isVerified(StreamlineUser user) {
        return isVerified(user.getUuid());
    }

    public boolean isVerified(String uuid) {
        for (String key : resource.singleLayerKeySet("users")) {
            if (key.equals(uuid)) return true;
        }

        return false;
    }
}
