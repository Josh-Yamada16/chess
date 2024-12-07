package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
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
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.playerName.equals(excludePlayer) && connections.get(excludePlayer).getGameID() == gameID) {
                    c.send(notification.getMessage());
                }
            } else {
                removeList.add(c);
            }
        }
        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.playerName);
        }
    }

    public void sendLoadGame(String player, LoadGameMessage gameMessage) throws IOException {
        var connection = connections.get(player);
        if (connection != null && connection.getSession().isOpen()) {
            String messageJson = new Gson().toJson(gameMessage);
            connection.getSession().getRemote().sendString(messageJson);
        }
    }
}
