package tv.quaint.discordmodule.server;

import net.streamline.api.modules.ModuleUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import tv.quaint.discordmodule.events.serverevents.ServerEventedEvent;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;


public abstract class SpigotServerEvent<T extends ServerEventedEvent<? extends Event>> extends ServerEvent<T> implements BaseEventListener {
    public SpigotServerEvent(String identifier) {
        super(identifier);
    }

    @BaseProcessor
    public void triggered(T event) {
        pushEvents(event);
    }

//    public String parseOnPlayer(Player player, String toParse, String identifier) {
//        toParse = parseOnEntity(player, toParse, identifier);
//
//        return toParse
//                .replace("%this_" + identifier + "_item_hand_main%", ModuleUtils.stripColor(player.getInventory().getItemInHand().getItemMeta().getDisplayName()))
//                .replace("%this_" + identifier + "_item_hand_off%", ModuleUtils.stripColor(player.getInventory().getItemInHand().getItemMeta().getDisplayName()))
//                .replace("%this_" + identifier + "_name_display%", ModuleUtils.stripColor(player.getDisplayName()))
//                .replace("%this_" + identifier + "_name_custom%", ModuleUtils.stripColor(player.getCustomName()))
//                ;
//    }
//
//    public String parseOnPlayer(Player player, String toParse) {
//        return parseOnPlayer(player, toParse, "player");
//    }
//
//    public String parseOnEntity(Entity entity, String toParse, String identifier) {
//        return toParse
//                .replace("%this_" + identifier + "_name%", entity.getName())
//                .replace("%this_" + identifier + "_uuid%", entity.getUniqueId().toString())
//                .replace("%this_" + identifier + "_world%", entity.getWorld().getName())
//                .replace("%this_" + identifier + "_type%", entity.getType().toString())
//                ;
//    }
//
//    public String parseOnEntity(Entity entity, String toParse) {
//        return parseOnEntity(entity, toParse, "entity");
//    }
}
