package tv.quaint.discord.voice;

import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import tv.quaint.events.DiscordVoiceStateUpdate;

public class StreamlineVoiceInterceptor implements VoiceDispatchInterceptor {
    @Override
    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate update) {
        // do nothing right now.
    }

    @Override
    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate update) {
        new DiscordVoiceStateUpdate(update).fire();
        return true;
    }
}
