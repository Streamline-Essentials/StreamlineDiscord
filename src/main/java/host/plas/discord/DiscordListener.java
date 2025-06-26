package host.plas.discord;

import gg.drak.thebase.objects.AtomicString;
import host.plas.DiscordModule;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import host.plas.events.streamline.bot.BotReadyEvent;
import host.plas.events.streamline.bot.posting.DiscordMessageEvent;
import singularity.modules.ModuleUtils;

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
        DiscordCommand command = DiscordHandler.getSlashCommand(event.getCommandIdLong());
        if (command == null) {
            DiscordModule.getInstance().logWarning("No command found for command id: " + event.getCommandIdLong());
            return;
        } else {
            DiscordModule.getInstance().logDebug("Command found for command id: " + event.getCommandIdLong());
        }

        AtomicString message = new AtomicString(command.getCommandIdentifier());

        event.getOptions().forEach(option -> {
            String current = message.get();
            message.set(current + " " + option.getAsString());
        });

        MessagedString messagedString = new MessagedString(event.getUser(), event.getChannel(), message.get());
        ReplyCallbackAction action = event.reply(command.execute(messagedString).getKey());
        if (command.isReplyEphemeral()) action = action.setEphemeral(true);
        action.queue();
    }
}
