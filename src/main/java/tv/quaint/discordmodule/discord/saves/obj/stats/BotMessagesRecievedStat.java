package tv.quaint.discordmodule.discord.saves.obj.stats;

import tv.quaint.discordmodule.discord.saves.obj.IntegerStat;

public class BotMessagesRecievedStat extends IntegerStat {
    public BotMessagesRecievedStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
