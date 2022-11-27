package tv.quaint.discordmodule.server.events.messaging.keyed;

import lombok.Getter;
import net.streamline.api.savables.users.StreamlinePlayer;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.discordmodule.server.events.messaging.MultiMessageKey;

import java.util.concurrent.ConcurrentSkipListSet;

public class AdvancementSetKey extends MultiMessageKey {
    @Getter
    private static final String registryValue = "advancement-set";

    public AdvancementSetKey(PlayerKey playerKey, String advancementTitle, String advancementDescription, String advancementCriteria) {
        super(registryValue);
        add(playerKey);
        add(new AdvancementTitleKey(advancementTitle));
        add(new AdvancementDescriptionKey(advancementDescription));
        add(new AdvancementCriteriaKey(advancementCriteria));
    }

    public AdvancementSetKey(StreamlinePlayer player, String advancementTitle, String advancementDescription, String advancementCriteria) {
        this(player.getUuid(), advancementTitle, advancementDescription, advancementCriteria);
    }

    public AdvancementSetKey(String playerUuid, String advancementTitle, String advancementDescription, String advancementCriteria) {
        this(new PlayerKey(playerUuid), advancementTitle, advancementDescription, advancementCriteria);
    }
}