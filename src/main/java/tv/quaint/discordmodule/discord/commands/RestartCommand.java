package tv.quaint.discordmodule.discord.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.MessagedString;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;

import java.util.concurrent.TimeUnit;

public class RestartCommand extends DiscordCommand {
    @Getter @Setter
    private String replyMessage;

    public RestartCommand() {
        super("restart",
                -1L,
                "restart", "res"
        );

        setReplyMessage(getResource().getOrSetDefault("messages.reply.message", "--file:restart-response.json"));
        loadFile("restart-response.json");
    }

    @Override
    public void init() {

    }

    @Override
    public void executeMore(MessagedString messagedString) {
        if (! DiscordHandler.init().completeOnTimeout(false, 15, TimeUnit.SECONDS).join()) {
            DiscordModule.getInstance().logWarning("Could not start Discord Module properly... (Timed out!)");
        }

        if (isJsonFile(getReplyMessage())) {
            String json = getJsonFromFile(getJsonFile(getReplyMessage()));
            DiscordMessenger.sendSimpleEmbed(messagedString.getChannel().getIdLong(), ModuleUtils.replaceAllPlayerBungee(ModuleUtils.getConsole(), json));
        } else {
            DiscordMessenger.sendMessage(messagedString.getChannel().getIdLong(), getReplyMessage());
        }
    }
}
