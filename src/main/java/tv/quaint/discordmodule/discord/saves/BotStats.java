package tv.quaint.discordmodule.discord.saves;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.discord.saves.obj.Stat;
import tv.quaint.discordmodule.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discordmodule.discord.saves.obj.stats.MessagesSentStat;
import tv.quaint.storage.resources.flat.FlatFileResource;
import tv.quaint.thebase.lib.leonhard.storage.Json;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BotStats extends FlatFileResource<Json> {
    @Getter @Setter
    private MessagesSentStat messagesSentStat;
    @Getter @Setter
    private MessagesRecievedStat messagesRecievedStat;
    @Getter @Setter
    private BotMessagesRecievedStat botMessagesRecievedStat;

    public BotStats() {
        super(Json.class, "bot-stats.json", DiscordModule.getInstance().getDataFolder(), false);

        setMessagesSentStat(new MessagesSentStat());
        setMessagesRecievedStat(new MessagesRecievedStat());
        setBotMessagesRecievedStat(new BotMessagesRecievedStat());
    }

    @Override
    public void sync() {
        getMessagesSentStat().save();
        getMessagesRecievedStat().save();
        getBotMessagesRecievedStat().save();

        super.sync();
    }
}
