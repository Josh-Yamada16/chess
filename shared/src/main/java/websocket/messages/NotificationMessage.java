package websocket.messages;

public class NotificationMessage extends ServerMessage{
    private final String message;

    public NotificationMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
