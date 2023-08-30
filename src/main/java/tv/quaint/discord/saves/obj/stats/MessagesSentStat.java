package tv.quaint.discord.saves.obj.stats;

import tv.quaint.discord.saves.obj.IntegerStat;

public class MessagesSentStat extends IntegerStat {
    public MessagesSentStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
