package tv.quaint.discordmodule.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import org.javacord.api.entity.activity.ActivityType;
import tv.quaint.discordmodule.DiscordModule;

public class BotLayout {
    @Getter @Setter
    private String token;
    @Getter @Setter
    private String prefix;
    @Getter @Setter
    private ActivityType activityType;
    @Getter @Setter
    private String activityValue;

    public BotLayout(String token, String commandPrefix, ActivityType activityType, String activityValue) {
        setToken(token);
        setPrefix(commandPrefix);
        setActivityType(activityType);
        setActivityValue(activityValue);
    }

    public void save() {
        DiscordModule.getConfig().saveBotLayout(this);
    }
}
