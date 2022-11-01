package tv.quaint.discordmodule.discord;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.streamline.api.modules.ModuleUtils;
import org.jetbrains.annotations.NotNull;
import tv.quaint.discordmodule.DiscordModule;

import java.util.List;

public class MessagedString {
    @NonNull @Getter @Setter
    private User author;
    @NonNull @Getter @Setter
    private MessageChannel channel;
    @NonNull @Getter @Setter
    private String totalMessage;

    public MessagedString(@NotNull User author, @NonNull MessageChannel channel, @NonNull final String totalMessage) {
        setAuthor(author);
        setChannel(channel);
        setTotalMessage(totalMessage);
    }

    @NonNull
    public String[] getTotalArgs() {
        return getTotalMessage().split(" ");
    }

    @NonNull
    public String getTotalBase() {
        return getTotalArgs()[0];
    }

    @NonNull
    public String getPrefix() {
        return DiscordModule.getConfig().getBotLayout().getPrefix();
    }

    public boolean hasPrefix() {
        return getTotalMessage().startsWith(getPrefix());
    }

    @NonNull
    public String getBase() {
        if (! hasPrefix()) return getTotalBase();
        return getTotalBase().substring(getPrefix().length());
    }

    @NonNull
    public String[] getCommandArgs() {
        if (getTotalArgs().length <= 1) return List.of("").toArray(new String[0]);
        return ModuleUtils.argsMinus(getTotalArgs(), 0);
    }

    public boolean hasCommandArgs() {
        return ! getCommandArgs()[0].equals("");
    }
}
