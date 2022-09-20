package tv.quaint.discordmodule.discord.saves;

import de.leonhard.storage.Json;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.FlatFileResource;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.Stat;
import tv.quaint.discordmodule.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesSentStat;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BotStats extends FlatFileResource<Json> {
    @Getter @Setter
    private ConcurrentSkipListMap<String, Stat<?>> loadedStats = new ConcurrentSkipListMap<>();

    public BotStats() {
        super(DiscordModule.getInstance(), Json.class, "bot-stats.json", false);

        loadStat(new MessagesSentStat());
        loadStat(new MessagesRecievedStat());
        loadStat(new BotMessagesRecievedStat());
    }

    public void loadStat(Stat<?> stat) {
        getLoadedStats().put(stat.getIdentifier(), stat);
    }
}
