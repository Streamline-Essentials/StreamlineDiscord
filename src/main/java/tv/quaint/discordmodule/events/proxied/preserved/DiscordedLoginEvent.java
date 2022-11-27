package tv.quaint.discordmodule.events.proxied.preserved;

import tv.quaint.discordmodule.events.proxied.DiscordEventMessageEvent;
import tv.quaint.discordmodule.server.ServerEvent;
import tv.quaint.discordmodule.server.events.streamline.LoginDSLEvent;

public class DiscordedLoginEvent extends DiscordEventMessageEvent<LoginDSLEvent> {
    public DiscordedLoginEvent(LoginDSLEvent event) {
        super(event);
    }
}
