package tv.quaint.discordmodule.server.events.streamline;

import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.server.DSLServerEvent;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;
import tv.quaint.discordmodule.server.events.messaging.keyed.PlayerKey;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

import java.util.Map;

public class LoginDSLEvent extends DSLServerEvent<LoginCompletedEvent> implements BaseEventListener {
    public LoginDSLEvent() {
        super("login");
        ModuleUtils.listen(this, DiscordModule.getInstance());
    }

    @BaseProcessor
    @Override
    public void onEvent(LoginCompletedEvent event) {
        pushEvents(event);
    }

    @Override
    public void pushEvents(LoginCompletedEvent event) {
        PlayerKey playerKey = new PlayerKey(event.getResource().getUuid());
        EventMessageInfo messageInfo = new EventMessageInfo(EventMessageInfo.EventType.LOGIN, playerKey);
        forwardMessage(messageInfo);
    }
}
