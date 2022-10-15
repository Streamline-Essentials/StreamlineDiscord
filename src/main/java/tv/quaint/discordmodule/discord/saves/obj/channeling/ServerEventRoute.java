package tv.quaint.discordmodule.discord.saves.obj.channeling;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.StorageUtils;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineConsole;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.utils.UserUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.events.ChanneledMessageEvent;
import tv.quaint.discordmodule.events.DiscordEventRouteCreateEvent;
import tv.quaint.discordmodule.server.ServerEvent;

import java.io.File;
import java.io.FileReader;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerEventRoute<T extends ServerEvent<?>> extends Route {
    @Getter @Setter
    private T event;
    @Getter @Setter
    private int subscribeIndex;

    public ServerEventRoute(String uuid, ChanneledFolder folder) {
        super(uuid, folder, true);
    }

    public ServerEventRoute(EndPoint input, ChanneledFolder folder, T event) {
        super(input, new EndPoint(EndPointType.DISCORD_TEXT, folder.getIdentifier().split("-", 2)[1],
                event.getDefaultMessageFormat("")), folder, true);
        if (event.isEnabled()) {
            int i = event.subscribe(
                    () -> getOutput().getToFormat(),
                    (e) -> {
                        bounceMessage(new RoutedUser(ModuleUtils.getConsole()), e);
                        return true;
                    }
            );
            setSubscribeIndex(i);
        } else {
            setSubscribeIndex(-1);
        }

        new DiscordEventRouteCreateEvent(this).fire();
    }

    @Override
    public void remove() {
        if (getParent() != null) getParent().unloadRoute(getUuid());

        try {
            getStorageResource().delete();
            dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
