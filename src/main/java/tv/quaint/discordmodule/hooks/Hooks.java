package tv.quaint.discordmodule.hooks;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Callable;

public enum Hooks {
    BASE(() -> HookHandler.getHook("streamline-base")),
    GROUPS(() -> HookHandler.getHook("streamline-groups")),
    MESSAGING(() -> HookHandler.getHook("streamline-messaging")),
    ;

    @Getter @Setter
    private Callable<DiscordHook<?>> callable;

    Hooks(Callable<DiscordHook<?>> callable) {
        setCallable(callable);
    }

    public DiscordHook<?> get() {
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
