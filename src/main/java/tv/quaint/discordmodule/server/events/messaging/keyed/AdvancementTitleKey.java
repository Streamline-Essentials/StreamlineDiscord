package tv.quaint.discordmodule.server.events.messaging.keyed;

import com.google.re2j.Matcher;
import lombok.Getter;
import net.streamline.api.objects.AtomicString;
import tv.quaint.discordmodule.server.events.messaging.MessageKey;
import tv.quaint.utils.MatcherUtils;

import java.util.List;

public class AdvancementTitleKey extends MessageKey<String> {
    @Getter
    private final static String registryValue = "advancement-title";

    public AdvancementTitleKey(String value) {
        super(registryValue);
        setValue(value);
    }

    @Override
    public String serialize() {
        return getRegistryKey() + "=" + getValue() + ";";
    }

    @Override
    public String deserialize(String value) {
        AtomicString r = new AtomicString("");

        Matcher matcherSmall = MatcherUtils.matcherBuilder("((.*?)[=](.*?)[;])", value);
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
