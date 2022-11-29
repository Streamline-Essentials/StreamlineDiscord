package tv.quaint.discordmodule.server.events.messaging.keyed;

import com.google.re2j.Matcher;
import lombok.Getter;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeathKeepInventoryKey extends MessageKey<Boolean> {
    @Getter
    private static final String registryValue = "death-keep-inventory";

    public DeathKeepInventoryKey(boolean value) {
        super(registryValue);
        setActualValue(value);
    }

    @Override
    public String serialize() {
        return getRegistryKey() + "+" + getValue() + "^";
    }

    @Override
    public Boolean deserialize(String value) {
        AtomicBoolean r = new AtomicBoolean(false);

        Matcher matcherSmall = MatcherUtils.matcherBuilder("((.*?)[+](.*?)[^])", value);
        List<String[]> groupsSmall = MatcherUtils.getGroups(matcherSmall, 3);
        groupsSmall.forEach(strgs -> {
            String key = strgs[1];
            String val = strgs[2];
            r.set(Boolean.parseBoolean(val));
        });

        return r.get();
    }

    @Override
    public MessageKey<Boolean> createCopy() {
        return new DeathKeepInventoryKey(this.getValue());
    }
}
