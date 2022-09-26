package tv.quaint.discordmodule.hooks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class DiscordHook<T> implements Comparable<DiscordHook<?>> {
    @Getter @Setter
    private int index;
    @Getter @Setter
    private Callable<T> hook;
    @Getter @Setter
    private String hookName;

    public DiscordHook(Callable<T> methodToGetHook, String hookName) {
        setIndex(HookHandler.getConfiguredHooks().size());
        setHook(methodToGetHook);
        setHookName(hookName);
    }

    public T get() {
        try {
            return hook.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isEnabled() {
        return HookHandler.isHooked(getHookName());
    }

    @Override
    public int compareTo(@NotNull DiscordHook<?> o) {
        return CharSequence.compare(getHookName(), o.getHookName());
    }
}
