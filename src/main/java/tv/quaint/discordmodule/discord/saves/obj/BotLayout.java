package tv.quaint.discordmodule.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Activity;
import tv.quaint.discordmodule.DiscordModule;

public class BotLayout {
    @Getter @Setter
    private String token;
    @Getter @Setter
    private String prefix;
    @Getter @Setter
    private Activity.ActivityType activityType;
    @Getter @Setter
    private String activityValue;
    @Getter @Setter
    private String avatarUrl;
    @Getter @Setter
    private boolean slashCommandsEnabled;

    public BotLayout(String token, String commandPrefix, Activity.ActivityType activityType, String activityValue, String avatarUrl, boolean slashCommandsEnabled) {
        setToken(token);
        setPrefix(commandPrefix);
        setActivityType(activityType);
        setActivityValue(activityValue);
        setAvatarUrl(avatarUrl);
        setSlashCommandsEnabled(slashCommandsEnabled);
    }

    public void save() {
        DiscordModule.getConfig().saveBotLayout(this);
    }
}
