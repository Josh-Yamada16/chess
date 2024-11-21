package websocket.messages;

public class LoadGameMessage<T> extends ServerMessage{
    private T game;

    public LoadGameMessage(ServerMessageType type, T game) {
        super(type);
        this.game = game;
    }
}
