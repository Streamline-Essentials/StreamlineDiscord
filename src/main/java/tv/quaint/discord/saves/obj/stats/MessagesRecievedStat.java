package tv.quaint.discord.saves.obj.stats;

import tv.quaint.discord.saves.obj.IntegerStat;

public class MessagesRecievedStat extends IntegerStat  {
    public MessagesRecievedStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
