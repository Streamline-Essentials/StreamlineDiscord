package tv.quaint.discordmodule.server.events.messaging;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;

public abstract class MessageKey<T> implements Comparable<MessageKey<?>> {
    @Getter
    private final String registryKey;
    @Getter
    private T value = null;

    public void setActualValue(T value) {
        this.value = value;
    }

    public MessageKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public <O> void setValue(O other) {
        try {
            setActualValue((T) other);
        } catch (Exception e) {
            DiscordModule.getInstance().logWarning("Failed to set value of MessageKey '" + registryKey + "' to '" + other + "' --> Not of correct class type!");
        }
    }

    @Override
    public int compareTo(@NotNull MessageKey<?> o) {
        return CharSequence.compare(registryKey, o.registryKey);
    }

    public boolean validate() {
        return value != null;
    }

    public void implement(String value) {
        try {
            setValue(deserialize(value));
        } catch (Exception e) {
            DiscordModule.getInstance().logWarning("Failed to implement value of MessageKey '" + registryKey + "' to '" + value + "' --> Not of correct class type!");
        }
    }
    
    public MessageKey<T> copyExact() {
        MessageKey<T> copy = createCopy();
        copy.setValue(value);
        return copy;
    }

    public abstract String serialize();

    public abstract T deserialize(String value);
    
    public abstract MessageKey<T> createCopy();
}
