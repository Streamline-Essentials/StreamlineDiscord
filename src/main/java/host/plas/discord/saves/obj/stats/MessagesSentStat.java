package host.plas.discord.saves.obj.stats;

import host.plas.discord.saves.obj.IntegerStat;

public class MessagesSentStat extends IntegerStat {
    public MessagesSentStat() {
        super(0);
    }

    @Override
    protected void updateValue() {
        // nothing
    }
}
