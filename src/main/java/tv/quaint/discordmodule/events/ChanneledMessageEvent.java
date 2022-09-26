package tv.quaint.discordmodule.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import tv.quaint.discordmodule.DiscordModule;
import tv.quaint.discordmodule.discord.saves.obj.channeling.EndPoint;

public class ChanneledMessageEvent extends ModuleEvent {
    @Getter @Setter
    private String message;
    @Getter @Setter
    private EndPoint input;
    @Getter @Setter
    private EndPoint desiredOutput;

    public ChanneledMessageEvent(String message, EndPoint input, EndPoint desiredOutput) {
        super(DiscordModule.getInstance());
        setMessage(message);
        setInput(input);
        setDesiredOutput(desiredOutput);
    }
}
