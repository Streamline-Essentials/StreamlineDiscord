package tv.quaint.discordmodule.hooks;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import tv.quaint.discordmodule.DiscordModule;

import java.util.concurrent.ConcurrentSkipListMap;

public class HookHandler {
    @Getter @Setter
    private static ConcurrentSkipListMap<DiscordHook<?>, Boolean> configuredHooks = new ConcurrentSkipListMap<>();

    public static void hookInto(DiscordHook<?> hook, boolean bool) {
        if (isHooked(hook.getHookName())) return;

        getConfiguredHooks().put(hook, bool);
    }

    public static void hookInto(DiscordHook<?> hook) {
        hookInto(hook, true);
    }

    public static void unhook(String hookName) {
        getConfiguredHooks().forEach((hook, aBoolean) -> {
            if (aBoolean) if (hook.getHookName().equals(hookName)) getConfiguredHooks().remove(hook);
        });
    }

    public static void unhook(DiscordHook<?> hook) {
        unhook(hook.getHookName());
    }

    public static boolean isHooked(String hookName) {
        for (DiscordHook<?> hook : getConfiguredHooks().keySet()) {
            if (hook.getHookName().equals(hookName)) return true;
        }

        return false;
    }

    public static boolean isCoreHooked() {
        return isHooked(SLAPI.class.getSimpleName());
    }

    public static boolean isGroupsHooked() {
        return isHooked(DiscordModule.getGroupsDependency().getApi().getClass().getSimpleName());
    }

    public static boolean isMessagingHooked() {
        return isHooked(DiscordModule.getMessagingDependency().getApi().getClass().getSimpleName());
    }
}
