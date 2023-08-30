package tv.quaint.discord.saves;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.DiscordModule;
import tv.quaint.discord.saves.obj.stats.BotMessagesRecievedStat;
import tv.quaint.discord.saves.obj.stats.MessagesRecievedStat;
import tv.quaint.discord.saves.obj.stats.MessagesSentStat;
import net.streamline.thebase.lib.leonhard.storage.Json;
import tv.quaint.storage.resources.flat.FlatFileResource;

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
