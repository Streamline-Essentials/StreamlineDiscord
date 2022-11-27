package tv.quaint.discordmodule.server.events.messaging;

import com.google.re2j.Matcher;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.objects.AtomicString;
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class MultiMessageKey extends MessageKey<ConcurrentSkipListSet<MessageKey<?>>> {
    @Getter @Setter
    private ConcurrentSkipListSet<MessageKey<?>> subKeys = new ConcurrentSkipListSet<>();

    public MultiMessageKey(String registryKey) {
        super(registryKey);
    }

    @Override
    public String serialize() {
        StringBuilder r = new StringBuilder("[");

        subKeys.forEach((key) -> {
            r.append(key.serialize());
        });

        r.append("!!]");

        return r.toString();
    }

    @Override
    public ConcurrentSkipListSet<MessageKey<?>> deserialize(String value) {
        ConcurrentSkipListSet<MessageKey<?>> r = new ConcurrentSkipListSet<>();

        Matcher matcherBig = MatcherUtils.matcherBuilder("(\\[(.*?)!!\\])", value);
        List<String[]> groupsBig = MatcherUtils.getGroups(matcherBig, 2);

        for (String[] stringArray : groupsBig) {
            String next = stringArray[1];
            Matcher matcherSmall = MatcherUtils.matcherBuilder("((.*?)[=](.*?)[;])", next);
            List<String[]> groupsSmall = MatcherUtils.getGroups(matcherSmall, 3);
            for (String[] stringArr : groupsSmall) {
                String whole = stringArr[0];
                String key = stringArr[1];
                MessageKey<?> messageKey = MessageKeyRegistry.get(key, whole);
                r.add(messageKey);
            }
        }

        return r;
    }

    @Override
    public MessageKey<ConcurrentSkipListSet<MessageKey<?>>> createCopy() {
        return new MultiMessageKey(this.getRegistryKey());
    }

    public void add(MessageKey<?> key) {
        subKeys.add(key);
    }

    public String getValue(String key) {
        AtomicString r = new AtomicString();
        subKeys.forEach((k) -> {
            if (k.getRegistryKey().equals(key)) {
                r.set(k.serialize());
            }
        });
        return r.get();
    }

    public String getValue(MessageKey<?> key) {
        return subKeys.contains(key) ? key.serialize() : subKeys.add(key) ? key.serialize() : null;
    }

    public <O> void setValue(String key, O value) {
        subKeys.forEach((k) -> {
            if (k.getRegistryKey().equals(key)) k.setValue(value);
        });
    }

    public <O> void setValue(MessageKey<?> key, O value) {
        key.setValue(value);
        subKeys.add(key);
    }

    public <O> void setValue(String key, String value) {
        subKeys.forEach((k) -> {
            if (k.getRegistryKey().equals(key)) {
                k.implement(value);
            }
        });
    }

    public <O> void setValue(MessageKey<?> key, String value) {
        key.implement(value);
        subKeys.add(key);
    }

    public MessageKey<?> get(String key) {
        AtomicReference<MessageKey<?>> r = new AtomicReference<>();
        subKeys.forEach((k) -> {
            if (k.getRegistryKey().equals(key)) {
                r.set(k);
            }
        });
        return r.get();
    }

    public void flush() {
        setSubKeys(new ConcurrentSkipListSet<>());
    }
}
