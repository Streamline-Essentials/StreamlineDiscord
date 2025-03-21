package host.plas.events.streamline.channels;

import lombok.Getter;
import lombok.Setter;
import host.plas.DiscordModule;
import host.plas.discord.saves.obj.channeling.EndPoint;
import singularity.events.modules.ModuleEvent;

@Setter
@Getter
public class ChanneledMessageEvent extends ModuleEvent {
    private String message;
    private EndPoint input;
    private EndPoint desiredOutput;

    public ChanneledMessageEvent(String message, EndPoint input, EndPoint desiredOutput) {
        super(DiscordModule.getInstance());
        setMessage(message);
        setInput(input);
        setDesiredOutput(desiredOutput);
    }
}
