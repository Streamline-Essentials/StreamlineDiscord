package tv.quaint.discordmodule.server.events.spigot;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.events.serverevents.DeathEventedEvent;
import tv.quaint.discordmodule.server.SpigotServerEvent;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;
import tv.quaint.discordmodule.server.events.messaging.keyed.DeathSetKey;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

public class DeathEventSpigot extends SpigotServerEvent<DeathEventedEvent> implements BaseEventListener {
    public DeathEventSpigot() {
        super("death");
        ModuleUtils.listen(this, DiscordModule.getInstance());
    }

    @Override
    public void pushEvents(DeathEventedEvent event) {
        PlayerDeathEvent ev = event.getEv();

        StreamlinePlayer player = ModuleUtils.getOrGetPlayer(ev.getEntity().getUniqueId().toString());
        if (player == null) return;

        DeathSetKey deathSetKey = new DeathSetKey(player, ev.getDeathMessage(), ev.getKeepLevel(), ev.getKeepInventory());

        EventMessageInfo messageInfo = new EventMessageInfo(EventMessageInfo.EventType.DEATH, deathSetKey);
        forwardMessage(messageInfo);
    }

    @BaseProcessor
    @Override
    public void onEvent(DeathEventedEvent event) {
        pushEvents(event);
    }
}
