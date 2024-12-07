package websocket;

import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void notify(NotificationMessage message);
}
