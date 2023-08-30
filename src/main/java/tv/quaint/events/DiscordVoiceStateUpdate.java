package tv.quaint.events;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.DiscordModule;

public class DiscordVoiceStateUpdate extends ModuleEvent {
    @Getter @Setter
    VoiceDispatchInterceptor.VoiceStateUpdate update;

    public DiscordVoiceStateUpdate(VoiceDispatchInterceptor.VoiceStateUpdate update) {
        super(DiscordModule.getInstance());

        setUpdate(update);
    }
}
