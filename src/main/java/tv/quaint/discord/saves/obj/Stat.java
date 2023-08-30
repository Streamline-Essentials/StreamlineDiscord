package tv.quaint.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.scheduler.ModuleRunnable;
import org.jetbrains.annotations.NotNull;
import tv.quaint.DiscordModule;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Stat<T> implements Comparable<String> {
    public class SyncTimer extends ModuleRunnable {
        public SyncTimer() {
            super(DiscordModule.getInstance(), 200, 1200);
        }

        @Override
        public void run() {
            updateValue();
            save();
        }
    }

    @Getter @Setter
    private SyncTimer syncTimer;

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private String key;
    @Getter @Setter
    private T defaultValue;
    @Getter @Setter
    private AtomicReference<T> value;

    public Stat(String identifier, T defaultValue) {
        this(defaultValue);
        setIdentifier(identifier);
    }

    public Stat(T defaultValue) {
        setSyncTimer(new SyncTimer());
        setIdentifier(getClass().getSimpleName());
        setKey("values." + getIdentifier());
        setDefaultValue(defaultValue);
        setValue(new AtomicReference<>(getDefaultValue()));
    }

    public T getOrGet() {
        T r = getValue().get();
        if (r != null) return r;

        DiscordModule.getBotStats().reloadResource();

        r = DiscordModule.getBotStats().getResource().getOrDefault(getKey(), getDefaultValue());
        getValue().set(r);
        return r;
    }

    public void save() {
        DiscordModule.getBotStats().getResource().set(getKey(), getValue().get());
    }

    protected abstract void updateValue();

    @Override
    public int compareTo(@NotNull String o) {
        return CharSequence.compare(getIdentifier(), o);
    }
}
