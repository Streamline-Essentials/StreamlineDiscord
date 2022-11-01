package tv.quaint.discordmodule.server.events.streamline;

import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.utils.UserUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPointType;
import tv.quaint.discordmodule.server.DSLServerEvent;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

import java.util.Map;

public class LoginDSLEvent extends DSLServerEvent<LoginCompletedEvent> implements BaseEventListener {
    public LoginDSLEvent() {
        super("login");
        ModuleUtils.listen(this, DiscordModule.getInstance());
        if (DiscordModule.getConfig().moduleForwardsEventsToProxy() && DiscordHandler.isBackEnd()) {
            String forwarded = DiscordModule.getMessages().forwardedStreamlineLogin();
            String toForward = getForwardMessage(forwarded);
            subscribe(
                    () -> toForward,
                    (s) -> {
                        if (UserUtils.getOnlineUsers().size() == 0) return false;
                        Map.Entry<String, StreamlinePlayer> playerEntry = UserUtils.getOnlinePlayers().firstEntry();
                        if (playerEntry == null) return false;
                        StreamlinePlayer player = playerEntry.getValue();
                        if (player == null) return false;
                        forwardMessage(s, EndPointType.SPECIFIC_NATIVE.toString(), player.getLatestServer());
                        forwardMessage(s, EndPointType.SPECIFIC_HANDLED.toString(), player.getLatestServer());
                        return true;
                    }
            );
        } else if (! DiscordHandler.isBackEnd()) {
            subscribe(
                    () -> DiscordModule.getMessages().forwardedStreamlineLogin(),
                    (s) -> {
                        if (UserUtils.getOnlineUsers().size() == 0) return false;
                        Map.Entry<String, StreamlinePlayer> playerEntry = UserUtils.getOnlinePlayers().firstEntry();
                        if (playerEntry == null) return false;
                        StreamlinePlayer player = playerEntry.getValue();
                        if (player == null) return false;
                        forwardMessage(s, EndPointType.GLOBAL_NATIVE.toString(), "");
                        return true;
                    }
            );
        }
    }

    @Override
    public String defaultMessageFormat() {
        return null;
    }

    @Override
    public String defaultJsonFile() {
        return "on-login.json";
    }

    @BaseProcessor
    @Override
    public void onEvent(LoginCompletedEvent event) {
        pushEvents(event);
    }

    @Override
    public String pass(String format, LoginCompletedEvent event) {
        StreamlinePlayer streamlinePlayer = event.getResource();
        if (streamlinePlayer != null) format = ModuleUtils.replaceAllPlayerBungee(streamlinePlayer, format);
        return format;
    }
}
