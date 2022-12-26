package tv.quaint.discordmodule.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.streamline.api.modules.ModuleUtils;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.events.BotReadyEvent;
import tv.quaint.discordmodule.events.DiscordMessageEvent;
import tv.quaint.objects.AtomicString;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
            AtomicString message = new AtomicString(command.getCommandIdentifier() + " ");

            event.getOptions().forEach(option -> {
                String current = message.get();
                if (option.getType() == OptionType.STRING) {
                    message.set(current + " " + option.getAsString());
                } else if (option.getType() == OptionType.INTEGER) {
                    message.set(current + " " + option.getAsInt());
                } else if (option.getType() == OptionType.BOOLEAN) {
                    message.set(current + " " + option.getAsBoolean());
                } else if (option.getType() == OptionType.USER) {
                    message.set(current + " " + option.getAsUser());
                } else if (option.getType() == OptionType.CHANNEL) {
                    message.set(current + " " + option.getAsChannel());
                } else if (option.getType() == OptionType.ROLE) {
                    message.set(current + " " + option.getAsRole());
                } else if (option.getType() == OptionType.MENTIONABLE) {
                    message.set(current + " " + option.getAsMentionable());
                } else if (option.getType() == OptionType.SUB_COMMAND) {
                    message.set(current + " " + option.getName());
                } else if (option.getType() == OptionType.SUB_COMMAND_GROUP) {
                    message.set(current + " " + option.getName());
                } else {
                    message.set(current + " " + option.getAsString());
                }
            });

            MessagedString messagedString = new MessagedString(event.getUser(), event.getChannel(), message.get());
            event.reply(command.execute(messagedString).getKey()).queue();
        });
    }
}
