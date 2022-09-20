package tv.quaint.discordmodule.hooks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class DiscordHook<T> implements Comparable<String> {
    @Getter @Setter
    private int index;
    @Getter @Setter
    private Callable<T> hook;

    public DiscordHook(Callable<T> methodToGetHook) {
        setIndex(HookHandler.getConfiguredHooks().size());
        setHook(methodToGetHook);
    }

    public T get() {
        try {
            return hook.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getHookName() {
        return get().getClass().getSimpleName();
    }

    @Override
    public int compareTo(@NotNull String o) {
        return CharSequence.compare(o, getHookName());
    }
}
