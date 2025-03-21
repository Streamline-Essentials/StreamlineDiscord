package host.plas.discord.saves;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.discord.saves.obj.stats.BotMessagesRecievedStat;
import host.plas.discord.saves.obj.stats.MessagesRecievedStat;
import host.plas.discord.saves.obj.stats.MessagesSentStat;
import tv.quaint.storage.resources.flat.simple.SimpleJson;

@Setter
@Getter
public class BotStats extends SimpleJson {
    private MessagesSentStat messagesSentStat;
    private MessagesRecievedStat messagesRecievedStat;
    private BotMessagesRecievedStat botMessagesRecievedStat;

    public BotStats() {
        super("bot-stats.json", DiscordModule.getInstance().getDataFolder(), false);

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
