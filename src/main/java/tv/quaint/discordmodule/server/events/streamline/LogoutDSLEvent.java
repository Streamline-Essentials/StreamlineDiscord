package tv.quaint.discordmodule.server.events.streamline;

import net.streamline.api.events.server.LogoutEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.server.DSLServerEvent;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;
import tv.quaint.discordmodule.server.events.messaging.keyed.PlayerKey;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;


public class LogoutDSLEvent extends DSLServerEvent<LogoutEvent> implements BaseEventListener {
    public LogoutDSLEvent() {
        super("logout");
        ModuleUtils.listen(this, DiscordModule.getInstance());
    }

    @BaseProcessor
    @Override
    public void onEvent(LogoutEvent event) {
        pushEvents(event);
    }

    @Override
    public void pushEvents(LogoutEvent event) {
        PlayerKey playerKey = new PlayerKey(event.getResource().getUuid());
        EventMessageInfo messageInfo = new EventMessageInfo(EventMessageInfo.EventType.LOGOUT, playerKey);
        forwardMessage(messageInfo);
    }
}
