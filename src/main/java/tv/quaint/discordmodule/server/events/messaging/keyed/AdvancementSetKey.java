package tv.quaint.discordmodule.server.events.messaging.keyed;

import net.streamline.api.savables.users.StreamlinePlayer;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.discordmodule.server.events.messaging.MultiMessageKey;

import java.util.concurrent.ConcurrentSkipListSet;

public class AdvancementSetKey extends MultiMessageKey {
    public AdvancementSetKey(StreamlinePlayer player, String advancementTitle, String advancementDescription, String advancementCriteria) {
        super("player");
        ConcurrentSkipListSet<MessageKey<?>> keys = new ConcurrentSkipListSet<>();
        keys.add(new PlayerKey(player.getUuid()));
        keys.add(new AdvancementTitleKey(advancementTitle));
        keys.add(new AdvancementDescriptionKey(advancementDescription));
        keys.add(new AdvancementCriteriaKey(advancementCriteria));
        setValue(keys);
    }
}