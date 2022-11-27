package tv.quaint.discordmodule.discord.messaging;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

public class BotMessageConfig {
    @Getter @Setter
    private boolean avatarChange = false;
    @Getter @Setter
    private String changeOnMessage = "";
    @Getter @Setter
    private String changeAfterMessage = "";

    public BotMessageConfig(JsonObject json) {
        JsonObject botObj = json.getAsJsonObject("bot");
        if (botObj != null) {
            JsonObject botAvatarObj = botObj.getAsJsonObject("avatar");
            if (botAvatarObj != null) {
                JsonObject changeAvatarObj = botAvatarObj.getAsJsonObject("change");
                if (changeAvatarObj != null) {
                    avatarChange = changeAvatarObj.get("enabled").getAsBoolean();
                    if (avatarChange) {
                        changeOnMessage = changeAvatarObj.get("to-on-message").getAsString();
                        changeAfterMessage = changeAvatarObj.get("to-after-message").getAsString();
                    }
                }
            }
        }
    }
}
