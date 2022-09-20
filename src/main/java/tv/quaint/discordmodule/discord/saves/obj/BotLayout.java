package tv.quaint.discordmodule.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.discordmodule.DiscordModule;

public class BotLayout {
    @Getter @Setter
    private String token;
    @Getter @Setter
    private String prefix;

    public BotLayout(String token, String commandPrefix, long controlCenter) {
        setToken(token);
        setPrefix(commandPrefix);
    }

    public BotLayout(String token, String commandPrefix) {
        this(token, commandPrefix, 0L);
    }

    public void save() {
        DiscordModule.getConfig().saveBotLayout(this);
    }
}
