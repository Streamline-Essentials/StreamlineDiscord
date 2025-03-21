package host.plas.discord.saves.obj.stats;

import host.plas.discord.saves.obj.IntegerStat;

public class MessagesRecievedStat extends IntegerStat  {
    public MessagesRecievedStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
