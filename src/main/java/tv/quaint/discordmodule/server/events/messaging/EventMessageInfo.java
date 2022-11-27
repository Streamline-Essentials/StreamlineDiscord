package tv.quaint.discordmodule.server.events.messaging;

import lombok.Getter;

public class EventMessageInfo {
    public enum EventType {
        LOGIN,
        LOGOUT,
        DEATH,
        ADVANCEMENT,
        ;
    }

    @Getter
    final EventType type;
    @Getter
    final MessageKey<?> messageKey;

    public EventMessageInfo(EventType type, MessageKey<?> messageKey) {
        this.type = type;
        this.messageKey = messageKey;
    }

    public void set(String value) {
        messageKey.implement(value);
    }

    public String read() {
        return messageKey.serialize();
    }
}
