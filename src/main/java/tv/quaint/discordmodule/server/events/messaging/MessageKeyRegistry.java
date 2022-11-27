package tv.quaint.discordmodule.server.events.messaging;

import lombok.Getter;

import java.util.concurrent.ConcurrentSkipListMap;

public class MessageKeyRegistry {
    @Getter
    static ConcurrentSkipListMap<String, MessageKey<?>> registry = new ConcurrentSkipListMap<>();

    public static MessageKey<?> get(String registryKey) {
        return registry.get(registryKey).createCopy();
    }

    public static <T> MessageKey<T> get(String registryKey, T value) {
        MessageKey<T> messageKey = (MessageKey<T>) registry.get(registryKey).createCopy();
        messageKey.setValue(value);
        return messageKey;
    }

    public static MessageKey<?> get(String registryKey, String value) {
        MessageKey<?> messageKey = registry.get(registryKey).createCopy();
        messageKey.implement(value);
        return messageKey;
    }

    public static void register(MessageKey<?> key) {
        registry.put(key.getRegistryKey(), key);
    }

    public static void unregister(String registryKey) {
        registry.remove(registryKey);
    }

    public static void unregister(MessageKey<?> key) {
        registry.remove(key.getRegistryKey());
    }
}
