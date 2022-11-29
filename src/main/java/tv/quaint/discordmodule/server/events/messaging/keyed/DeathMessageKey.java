package tv.quaint.discordmodule.server.events.messaging.keyed;

import com.google.re2j.Matcher;
import lombok.Getter;
import net.streamline.api.objects.AtomicString;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.utils.MatcherUtils;

import java.util.List;

public class DeathMessageKey extends MessageKey<String> {
    @Getter
    private static final String registryValue = "death-message";

    public DeathMessageKey(String value) {
        super(registryValue);
        setActualValue(value);
    }

    @Override
    public String serialize() {
        return getRegistryKey() + "+" + getValue() + "^";
    }

    @Override
    public String deserialize(String value) {
        AtomicString r = new AtomicString("");

        Matcher matcherSmall = MatcherUtils.matcherBuilder("((.*?)[+](.*?)[^])", value);
        List<String[]> groupsSmall = MatcherUtils.getGroups(matcherSmall, 3);
        groupsSmall.forEach(strgs -> {
            String key = strgs[1];
            String val = strgs[2];
            r.set(val);
        });

        return r.get();
    }

    @Override
    public MessageKey<String> createCopy() {
        return new DeathMessageKey(this.getValue());
    }
}
