package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String player, Session session, Integer gameID) {
        var connection = new Connection(player, session, gameID);
        connections.put(player, connection);
    }

    public void remove(String player) {
        connections.remove(player);
    }

    public void broadcast(String excludePlayer, ServerMessage notification, Integer gameID) throws IOException {
        for (var c : connections.values()) {
            if (c.session != null && c.session.isOpen()) {
                if (!c.playerName.equals(excludePlayer) && connections.get(excludePlayer).getGameID() == gameID) {
                    c.send(new Gson().toJson(notification));
                }
            }
        }
    }

    public void sendLoadGame(String player, ServerMessage message) throws IOException {
        var connection = connections.get(player);
        boolean yes = connection.getSession().isOpen();
        if (connection != null && yes) {
            String messageJson = new Gson().toJson(message);
            connection.getSession().getRemote().sendString(messageJson);
        }
    }
}
