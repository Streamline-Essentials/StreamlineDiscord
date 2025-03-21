package host.plas.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import host.plas.DiscordModule;
import singularity.scheduler.ModuleRunnable;

import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
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

    private SyncTimer syncTimer;

    private String identifier;
    private String key;
    private T defaultValue;
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
