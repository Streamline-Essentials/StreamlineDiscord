package host.plas.discord.saves.obj.stats;

import host.plas.discord.saves.obj.IntegerStat;

public class BotMessagesRecievedStat extends IntegerStat {
    public BotMessagesRecievedStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
