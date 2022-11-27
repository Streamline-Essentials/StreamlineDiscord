package tv.quaint.discordmodule.server.events.messaging.keyed;

import net.streamline.api.savables.users.StreamlinePlayer;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.discordmodule.server.events.messaging.MultiMessageKey;

import java.util.concurrent.ConcurrentSkipListSet;

public class DeathSetKey extends MultiMessageKey {
    public DeathSetKey(StreamlinePlayer player, String deathMessage, boolean keepExperience, boolean keepInventory) {
        super("player");
        ConcurrentSkipListSet<MessageKey<?>> keys = new ConcurrentSkipListSet<>();
        keys.add(new PlayerKey(player.getUuid()));
        keys.add(new DeathMessageKey(deathMessage));
        keys.add(new DeathKeepExperienceKey(keepExperience));
        keys.add(new DeathKeepInventoryKey(keepInventory));
        setValue(keys);
    }
}
