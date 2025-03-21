package host.plas.discord.saves.obj;

public abstract class IntegerStat extends Stat<Integer> {
    public IntegerStat(String identifier, Integer defaultValue) {
        super(identifier, defaultValue);
    }

    public IntegerStat(Integer defaultValue) {
        super(defaultValue);
    }

    public int increment(int amount) {
        getValue().set(getOrGet() + amount);

        return getOrGet();
    }

    public int increment() {
        return increment(1);
    }

    public int decrement(int amount) {
        getValue().set(getOrGet() - amount);

        return getOrGet();
    }

    public int decrement() {
        return decrement(1);
    }
}
