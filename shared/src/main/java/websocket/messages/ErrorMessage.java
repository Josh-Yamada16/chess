package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private String message;

    public ErrorMessage(ServerMessageType type, String errorMessage) {
        super(type);
        this.message = errorMessage;
    }

    public String getMessage() {
        return message;
    }
}
