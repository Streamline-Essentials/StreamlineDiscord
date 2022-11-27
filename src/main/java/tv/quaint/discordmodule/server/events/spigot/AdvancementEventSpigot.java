package tv.quaint.discordmodule.server.events.spigot;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.events.serverevents.AdvancementEventedEvent;
import tv.quaint.discordmodule.server.SpigotServerEvent;
import tv.quaint.discordmodule.server.events.messaging.EventMessageInfo;
import tv.quaint.discordmodule.server.events.messaging.keyed.AdvancementSetKey;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

public class AdvancementEventSpigot extends SpigotServerEvent<AdvancementEventedEvent> implements BaseEventListener {
    public AdvancementEventSpigot() {
        super("advancement");
        ModuleUtils.listen(this, DiscordModule.getInstance());
    }

    @Override
    public void pushEvents(AdvancementEventedEvent event) {
        PlayerAdvancementDoneEvent ev = event.getEv();

        StreamlinePlayer player = ModuleUtils.getOrGetPlayer(ev.getPlayer().getUniqueId().toString());
        if (player == null) return;

        Advancement advancement = ev.getAdvancement();
        AdvancementDisplay display = advancement.getDisplay();

        AdvancementSetKey advancementSetKey = new AdvancementSetKey(player, advancement.getDisplay().getTitle(),
                advancement.getDisplay().getDescription(), ModuleUtils.getListAsFormattedString(advancement.getCriteria().stream().toList()));

        EventMessageInfo messageInfo = new EventMessageInfo(EventMessageInfo.EventType.ADVANCEMENT, advancementSetKey);
        forwardMessage(messageInfo);
    }

    @BaseProcessor
    @Override
    public void onEvent(AdvancementEventedEvent event) {
        pushEvents(event);
    }
}
