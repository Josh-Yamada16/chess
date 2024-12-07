package websocket;

import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage message);
}
