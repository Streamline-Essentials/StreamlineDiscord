package tv.quaint.discord.saves.obj;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Activity;
import tv.quaint.DiscordModule;

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
    @Getter @Setter
    private long mainGuildId;

    public BotLayout(String token, String commandPrefix, Activity.ActivityType activityType, String activityValue, String avatarUrl, boolean slashCommandsEnabled, long mainGuildId) {
        setToken(token);
        setPrefix(commandPrefix);
        setActivityType(activityType);
        setActivityValue(activityValue);
        setAvatarUrl(avatarUrl);
        setSlashCommandsEnabled(slashCommandsEnabled);
        setMainGuildId(mainGuildId);
    }

    public void save() {
        DiscordModule.getConfig().saveBotLayout(this);
    }
}
