package tv.quaint.discordmodule.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.streamline.api.modules.ModuleUtils;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.events.BotReadyEvent;
import tv.quaint.discordmodule.events.DiscordMessageEvent;

import java.util.Optional;

public class DiscordListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        ModuleUtils.fireEvent(new DiscordMessageEvent(new MessagedString(event.getAuthor(), event.getChannel(), event.getMessage().getContentRaw())));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        new BotReadyEvent(DiscordHandler.getUser(event.getJDA().getSelfUser().getIdLong())).fire();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        Optional.ofNullable(DiscordHandler.getSlashCommand(commandName)).ifPresent(command -> {
            MessagedString messagedString = new MessagedString(event.getUser(), event.getChannel(),
                    command.getCommandIdentifier() + " " + event.getCommandString());
            event.reply(command.execute(messagedString).getKey()).queue();
        });
    }
}
