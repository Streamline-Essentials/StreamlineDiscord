package tv.quaint.discordmodule.server.events.messaging.keyed;

import lombok.Getter;
import net.streamline.api.savables.users.StreamlinePlayer;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.discordmodule.server.events.messaging.MultiMessageKey;

import java.util.concurrent.ConcurrentSkipListSet;

public class DeathSetKey extends MultiMessageKey {
    @Getter
    private static final String registryValue = "death-set";

    public DeathSetKey(PlayerKey key, String deathMessage, boolean keepExperience, boolean keepInventory) {
        super(registryValue);
        add(key);
        add(new DeathMessageKey(deathMessage));
        add(new DeathKeepExperienceKey(keepExperience));
        add(new DeathKeepInventoryKey(keepInventory));
    }

    public DeathSetKey(StreamlinePlayer player, String deathMessage, boolean keepExperience, boolean keepInventory) {
        this(player.getUuid(), deathMessage, keepExperience, keepInventory);
    }

    public DeathSetKey(String playerUuid, String deathMessage, boolean keepExperience, boolean keepInventory) {
        this(new PlayerKey(playerUuid), deathMessage, keepExperience, keepInventory);
    }
}
