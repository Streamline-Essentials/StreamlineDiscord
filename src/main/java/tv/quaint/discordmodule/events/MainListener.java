package tv.quaint.discordmodule.events;

import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.DiscordCommand;
import tv.quaint.discordmodule.discord.DiscordHandler;
import tv.quaint.discordmodule.discord.messaging.DiscordMessenger;
import tv.quaint.discordmodule.events.DiscordCommandEvent;
import tv.quaint.discordmodule.events.DiscordMessageEvent;

import java.util.concurrent.CompletableFuture;

public class MainListener implements StreamlineListener {
    public MainListener() {
        DiscordModule.getInstance().logInfo(getClass().getSimpleName() + " is now registered!");
    }

    @EventProcessor
    public void DiscordMessageEvent(DiscordMessageEvent event) {
        if (event instanceof DiscordCommandEvent) return;

        if (DiscordHandler.hasVerification(event.getMessage().getTotalMessage())) {
            if (DiscordHandler.verifyUser(event.getMessage().getSender().getId(), event.getMessage().getTotalMessage())) {

                StreamlineUser user = ModuleUtils.getOrGetUser(DiscordModule.getVerifiedUsers().discordIdToUUID(event.getMessage().getSender().getId()));
                if (user == null) {
                    DiscordModule.getInstance().logWarning("Verified Discord ID '" + event.getMessage().getSender().getId() + "', but the associated StreamlineUser is 'null'!");
                    return;
                }

                ModuleUtils.sendMessage(user, DiscordModule.getMessages().completedMinecraft());

                if (DiscordModule.isJsonFile(DiscordModule.getMessages().completedDiscord())) {
                    String fileName = DiscordModule.getJsonFile(DiscordModule.getMessages().completedDiscord());

                    CompletableFuture.runAsync(() -> {
                        DiscordModule.loadFile(fileName);
                    }).join();

                    DiscordMessenger.sendSimpleEmbed(event.getMessage().getChannel().getId(), DiscordModule.getJsonFromFile(fileName));
                } else {
                    DiscordMessenger.sendMessage(event.getMessage().getChannel().getId(), DiscordModule.getMessages().completedDiscord());
                }
                return;
            }
        }

        if (! event.getMessage().hasPrefix()) {
            return;
        }

        DiscordCommand command = DiscordHandler.getCommandByAlias(event.getMessage().getBase());
        if (command == null) {
            DiscordModule.getInstance().logWarning("Could not get DiscordCommand with alias of '" + event.getMessage().getBase() + "'.");
            return;
        }

        ModuleUtils.fireEvent(new DiscordCommandEvent(event.getMessage(), command));
    }

    @EventProcessor
    public void DiscordMessageEvent(DiscordCommandEvent event) {
        DiscordModule.getInstance().logInfo("Executing command '" + event.getCommand().getCommandIdentifier() + "'...!");
        event.getCommand().execute(event.getMessage());
    }
}
